package com.spottrack.platform.monitoring.infrastructure.pairing;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.read.ListAppender;
import com.spottrack.platform.monitoring.application.internal.commandcervices.SensorPairingCommandServiceImpl;
import com.spottrack.platform.monitoring.application.ports.SensorPairingSettings;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.commands.ExpireDueSensorPairingsCommand;
import com.spottrack.platform.monitoring.domain.model.commands.IssueSensorPairingCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MarkSensorPairingUsedCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.IssuedSensorPairing;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.monitoring.testsupport.InMemorySensorPairingRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.AfterEach;
import org.springframework.transaction.support.TransactionOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PairingSecretNeverLoggedTest {

    private static final String SENSOR_UUID = "066dd222-31ed-496a-bddf-e4711b0ee957";
    private static final MotionSensorId SENSOR = new MotionSensorId(SENSOR_UUID);
    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 15, 12, 0);
    private static final String CA = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

    private final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private Level previousLevel;

    private final AtomicReference<LocalDateTime> now = new AtomicReference<>(START);
    private final Clock clock = new Clock() {
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public java.time.Instant instant() { return now.get().toInstant(ZoneOffset.UTC); }
    };

    private InMemorySensorPairingRepository pairings;
    private SensorPairingCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        previousLevel = rootLogger.getLevel();
        rootLogger.setLevel(Level.TRACE);
        logs.start();
        rootLogger.addAppender(logs);

        var sensors = mock(MotionSensorRepository.class);
        when(sensors.findByMotionSensorId(SENSOR)).thenReturn(Optional.of(
                new MotionSensor(1L, SENSOR, new EquipmentId("eq-1"), START, false, START, null, null)));
        pairings = new InMemorySensorPairingRepository();
        service = new SensorPairingCommandServiceImpl(sensors, pairings, new LoggingBrokerCredentialProvisioner(),
                new BCryptPairingSecretHasher(),
                new SensorPairingSettings(Duration.ofMinutes(15), "mqtt.example.com:8883", CA),
                profile -> true, TransactionOperations.withoutTransaction(), clock);
    }

    @AfterEach
    void tearDown() {
        rootLogger.detachAppender(logs);
        rootLogger.setLevel(previousLevel);
        logs.stop();
    }

    private IssuedSensorPairing issue() {
        var result = service.handle(new IssueSensorPairingCommand(SENSOR_UUID));
        return ((Result.Success<IssuedSensorPairing, ApplicationError>) result).value();
    }

    private String allLoggedText() {
        var text = new StringBuilder();
        for (var event : logs.list) {
            text.append(event.getFormattedMessage()).append('\n');
            if (event.getThrowableProxy() != null) text.append(ThrowableProxyUtil.asString(event.getThrowableProxy()));
            event.getArgumentArray();
            if (event.getArgumentArray() != null) {
                for (var arg : event.getArgumentArray()) text.append(arg).append('\n');
            }
        }
        return text.toString();
    }

    @Test
    void thePairingLifecycleNeverWritesTheSecretToTheLogs() {
        var secret = issue().qrPayload().secret();
        service.handle(new MarkSensorPairingUsedCommand(SENSOR));
        issue();
        now.set(START.plusHours(1));
        service.handle(new ExpireDueSensorPairingsCommand());

        assertFalse(logs.list.isEmpty(), "the flow is expected to log something");
        assertFalse(allLoggedText().contains(secret));
    }

    @Test
    void anExpiredQrFlowNeverWritesTheSecretToTheLogs() {
        var secret = issue().qrPayload().secret();
        now.set(START.plusHours(1));

        service.handle(new MarkSensorPairingUsedCommand(SENSOR));
        service.handle(new ExpireDueSensorPairingsCommand());

        assertFalse(allLoggedText().contains(secret));
    }

    @Test
    void loggingProvisionerOnlyLogsTheDeviceId() {
        var provisioner = new LoggingBrokerCredentialProvisioner();
        var secret = "s3cr3t-value-that-must-not-leak-1234";

        provisioner.provision("dev-1", secret);
        provisioner.revoke("dev-1");

        assertEquals(2, logs.list.size());
        assertTrue(allLoggedText().contains("dev-1"));
        assertFalse(allLoggedText().contains(secret));
    }

    @Test
    void issuedPairingToStringDoesNotExposeTheSecret() {
        var issued = issue();
        var secret = issued.qrPayload().secret();

        assertFalse(issued.toString().contains(secret));
        assertFalse(issued.qrPayload().toString().contains(secret));
        assertTrue(issued.qrPayload().toString().contains("secret=****"));
    }

    @Test
    void issueCommandHasNoSecretToExpose() {
        var command = new IssueSensorPairingCommand(SENSOR_UUID);

        assertEquals("IssueSensorPairingCommand[motionSensorId=" + SENSOR_UUID + ", payloadProfile=null]", command.toString());
    }

    @Test
    void persistedPairingKeepsOnlyABcryptHashOfTheSecret() {
        var secret = issue().qrPayload().secret();

        var stored = pairings.all().getFirst();
        assertNotEquals(secret, stored.getSecretHash());
        assertTrue(stored.getSecretHash().startsWith("$2"));
        assertFalse(stored.toString().contains(secret));
    }
}
