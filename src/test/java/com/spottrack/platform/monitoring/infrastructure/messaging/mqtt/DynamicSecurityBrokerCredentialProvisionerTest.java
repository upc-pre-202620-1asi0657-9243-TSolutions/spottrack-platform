package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.read.ListAppender;
import com.spottrack.platform.monitoring.application.ports.BrokerProvisioningException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicSecurityBrokerCredentialProvisionerTest {
    private static final String B1 = "ssl://10.0.0.1:8883";
    private static final String B2 = "ssl://10.0.0.2:8883";
    private static final String B3 = "ssl://10.0.0.3:8883";
    private static final String DEVICE = "066dd222-31ed-496a-bddf-e4711b0ee957";
    private static final String SECRET = "s3cr3t-Zk9x-pairing-secret";

    private final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private Level previousLevel;

    private FakeDynsecBrokers brokers;
    private DynamicSecurityBrokerCredentialProvisioner provisioner;

    @BeforeEach
    void setUp() {
        previousLevel = rootLogger.getLevel();
        rootLogger.setLevel(Level.TRACE);
        logs.start();
        rootLogger.addAppender(logs);
        brokers = new FakeDynsecBrokers(B1, B2, B3);
        provisioner = provisionerWith(2, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        rootLogger.detachAppender(logs);
        logs.stop();
        rootLogger.setLevel(previousLevel);
    }

    private DynamicSecurityBrokerCredentialProvisioner provisionerWith(int maxAttempts, Duration totalTimeout) {
        return new DynamicSecurityBrokerCredentialProvisioner(brokers,
                new DynsecProvisionerSettings(List.of(B1, B2, B3), "sensor", maxAttempts, totalTimeout, Duration.ZERO));
    }

    @Test
    void provisionCreatesTheAccountWithTheSensorRoleOnEveryBroker() {
        provisioner.provision(DEVICE, SECRET);

        for (var broker : List.of(B1, B2, B3)) {
            assertTrue(brokers.hasClient(broker, DEVICE));
            assertEquals(SECRET, brokers.passwordOf(broker, DEVICE));
            assertEquals("sensor", brokers.roleOf(broker, DEVICE));
        }
    }

    @Test
    void provisionSendsDeleteCreateAndRoleInOneMessagePerBroker() {
        provisioner.provision(DEVICE, SECRET);

        assertEquals(3, brokers.calls.size());
        for (var call : brokers.calls) {
            assertEquals(List.of("deleteClient", "createClient", "addClientRole"),
                    call.commands().stream().map(DynsecCommand::name).toList());
        }
    }

    @Test
    void reprovisioningReplacesThePreviousSecretOnEveryBroker() {
        provisioner.provision(DEVICE, "old-secret-value");
        provisioner.provision(DEVICE, SECRET);

        for (var broker : List.of(B1, B2, B3)) {
            assertEquals(SECRET, brokers.passwordOf(broker, DEVICE));
        }
    }

    @Test
    void provisionFailureOnTheSecondBrokerRollsBackTheFirst() {
        brokers.failCommand(B2, "createClient", "Internal error.");

        var error = assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(DEVICE, SECRET));

        assertTrue(error.getMessage().contains(B2));
        for (var broker : List.of(B1, B2, B3)) {
            assertFalse(brokers.hasClient(broker, DEVICE), "no broker may keep the account: " + broker);
        }
    }

    @Test
    void rollbackIsAlsoSentToTheBrokerThatFailed() {
        brokers.failCommand(B2, "addClientRole", "Internal error.");

        assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(DEVICE, SECRET));

        var deletesToB2 = brokers.calls.stream()
                .filter(call -> call.brokerUrl().equals(B2) && call.commands().size() == 1).count();
        assertEquals(1, deletesToB2);
        assertFalse(brokers.hasClient(B2, DEVICE));
    }

    @Test
    void failedRollbackIsReportedInTheException() {
        brokers.failTransport(B3, () -> new DynsecTransportException("no response"));

        var error = assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(DEVICE, SECRET));

        assertTrue(error.getMessage().contains("may still exist on"));
        assertFalse(brokers.hasClient(B1, DEVICE));
        assertFalse(brokers.hasClient(B2, DEVICE));
    }

    @Test
    void failedReissueLeavesTheDeviceWithoutCredentialNotWithAMixedOne() {
        provisioner.provision(DEVICE, "old-secret-value");
        brokers.failCommand(B2, "createClient", "Internal error.");

        assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(DEVICE, SECRET));

        for (var broker : List.of(B1, B2, B3)) {
            assertFalse(brokers.hasClient(broker, DEVICE));
        }
    }

    @Test
    void transportErrorIsRetriedAndThenSucceeds() {
        var attempts = new int[1];
        var flaky = new DynsecBrokerClient() {
            @Override
            public List<DynsecResult> execute(String url, List<DynsecCommand> commands) throws DynsecTransportException {
                if (url.equals(B2) && attempts[0]++ == 0) throw new DynsecTransportException("connect timed out");
                return brokers.execute(url, commands);
            }
        };
        var withRetry = new DynamicSecurityBrokerCredentialProvisioner(flaky,
                new DynsecProvisionerSettings(List.of(B1, B2, B3), "sensor", 2, Duration.ofSeconds(5), Duration.ZERO));

        withRetry.provision(DEVICE, SECRET);

        assertTrue(brokers.hasClient(B2, DEVICE));
        assertEquals(2, attempts[0]);
    }

    @Test
    void transportErrorIsGivenUpAfterTheConfiguredAttempts() {
        brokers.failTransport(B2, () -> new DynsecTransportException("connect timed out"));

        assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(DEVICE, SECRET));

        var toB2 = brokers.calls.stream().filter(call -> call.brokerUrl().equals(B2)).count();
        // 2 attempts to provision + 2 attempts to roll back
        assertEquals(4, toB2);
    }

    @Test
    void errorAnsweredByTheBrokerIsNotRetried() {
        brokers.failCommand(B1, "createClient", "Internal error.");

        assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(DEVICE, SECRET));

        var provisionCallsToB1 = brokers.calls.stream()
                .filter(call -> call.brokerUrl().equals(B1) && call.commands().size() == 3).count();
        assertEquals(1, provisionCallsToB1);
    }

    @Test
    void aBrokerThatNeverAnswersMakesProvisionFailWithinTheTotalTimeoutAndRollsBack() {
        brokers.hang(B2);
        var impatient = provisionerWith(2, Duration.ofMillis(300));

        var start = System.nanoTime();
        var error = assertThrows(BrokerProvisioningException.class, () -> impatient.provision(DEVICE, SECRET));
        var elapsed = Duration.ofNanos(System.nanoTime() - start);

        assertTrue(error.getMessage().contains("timed out"));
        assertTrue(elapsed.compareTo(Duration.ofSeconds(5)) < 0, "took " + elapsed);
        assertFalse(brokers.hasClient(B1, DEVICE));
        assertFalse(brokers.hasClient(B3, DEVICE));
    }

    @Test
    void revokeRemovesTheAccountFromEveryBroker() {
        provisioner.provision(DEVICE, SECRET);

        provisioner.revoke(DEVICE);

        for (var broker : List.of(B1, B2, B3)) {
            assertFalse(brokers.hasClient(broker, DEVICE));
        }
    }

    @Test
    void revokeIsIdempotentAndToleratesAnAccountThatDoesNotExist() {
        provisioner.revoke(DEVICE);
        provisioner.provision(DEVICE, SECRET);
        provisioner.revoke(DEVICE);
        provisioner.revoke(DEVICE);

        for (var broker : List.of(B1, B2, B3)) {
            assertFalse(brokers.hasClient(broker, DEVICE));
        }
    }

    @Test
    void revokeTriesEveryBrokerEvenWhenOneFailsAndThenReports() {
        provisioner.provision(DEVICE, SECRET);
        brokers.failTransport(B1, () -> new DynsecTransportException("no response"));

        var error = assertThrows(BrokerProvisioningException.class, () -> provisioner.revoke(DEVICE));

        assertTrue(error.getMessage().contains(B1));
        assertFalse(brokers.hasClient(B2, DEVICE));
        assertFalse(brokers.hasClient(B3, DEVICE));
        assertTrue(brokers.hasClient(B1, DEVICE));

        brokers.stopFailingTransport(B1);
        provisioner.revoke(DEVICE);
        assertFalse(brokers.hasClient(B1, DEVICE));
    }

    @Test
    void revokeAnsweredWithAnotherErrorFails() {
        brokers.failCommand(B2, "deleteClient", "Internal error.");

        assertThrows(BrokerProvisioningException.class, () -> provisioner.revoke(DEVICE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "dev 1", "dev/1", "dev+", "dev#", "a$b", "spottrack-backend", "spottrack-dynsec-admin",
            "x'; drop", "ñandú", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void invalidOrReservedDeviceIdsAreRejectedBeforeTouchingAnyBroker(String deviceId) {
        assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(deviceId, SECRET));
        assertThrows(BrokerProvisioningException.class, () -> provisioner.revoke(deviceId));

        assertTrue(brokers.calls.isEmpty());
    }

    @Test
    void nullDeviceIdAndEmptySecretAreRejected() {
        assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(null, SECRET));
        assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(DEVICE, " "));
        assertTrue(brokers.calls.isEmpty());
    }

    @Test
    void theSecretNeverAppearsInLogsExceptionsOrToString() {
        brokers.failCommand(B2, "createClient", "Internal error.");
        brokers.failTransport(B3, () -> new DynsecTransportException("no response"));

        var error = assertThrows(BrokerProvisioningException.class, () -> provisioner.provision(DEVICE, SECRET));
        brokers.stopFailingTransport(B3);
        brokers.clearCommandFailures();
        provisioner.provision(DEVICE, SECRET);
        provisioner.revoke(DEVICE);

        assertFalse(error.getMessage().contains(SECRET));
        assertFalse(DynsecCommand.createClient(DEVICE, SECRET).toString().contains(SECRET));
        for (var event : logs.list) {
            assertFalse(event.getFormattedMessage().contains(SECRET), event.getFormattedMessage());
            if (event.getThrowableProxy() != null) {
                assertFalse(ThrowableProxyUtil.asString(event.getThrowableProxy()).contains(SECRET));
            }
        }
        assertFalse(logs.list.isEmpty());
    }
}
