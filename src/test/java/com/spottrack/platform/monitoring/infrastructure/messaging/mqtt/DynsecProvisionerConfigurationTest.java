package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.spottrack.platform.monitoring.application.ports.BrokerCredentialProvisioner;
import com.spottrack.platform.monitoring.infrastructure.pairing.LoggingBrokerCredentialProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynsecProvisionerConfigurationTest {
    private static final String CA_PEM = """
            -----BEGIN CERTIFICATE-----
            MIIBeDCCAR6gAwIBAgIUGYLYn17ns0IH3DwYJxNi52hxHgkwCgYIKoZIzj0EAwIw
            ITEfMB0GA1UEAwwWU3BvdFRyYWNrIHRlc3QgTVFUVCBDQTAgFw0yNjExMTQxNTUz
            NDRaGA8yMTI2MTAyMTE1NTM0NFowITEfMB0GA1UEAwwWU3BvdFRyYWNrIHRlc3Qg
            TVFUVCBDQTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABGw0D6yvxMo1ySboi1JB
            FpmwtmdY7v76Z6TpborpH+DyrCR/uCJkUl2O6cl1e3gmVXHvEkSY73E5Z0bVWGqb
            /Z2jMjAwMB0GA1UdDgQWBBT0sPk5r9dyTvYxJcKLxnmiqXRjXjAPBgNVHRMBAf8E
            BTADAQH/MAoGCCqGSM49BAMCA0gAMEUCIQCllutOvtc7jJAYbd329jCXB6wKEDu0
            7FQZr4fzX17n/QIgFbMmLUwvKi7hws8fr7317QOkPZ1VDRI7/6keBuPqXNY=
            -----END CERTIFICATE-----
            """;
    private static final String CA_BASE64 = Base64.getEncoder().encodeToString(CA_PEM.getBytes(StandardCharsets.UTF_8));
    private static final String BROKERS = "ssl://10.0.0.1:8883,ssl://10.0.0.2:8883,ssl://10.0.0.3:8883";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(DynsecProvisionerConfiguration.class, LoggingBrokerCredentialProvisioner.class);

    private ApplicationContextRunner dynsec(String... overrides) {
        var runnerWithBase = runner.withPropertyValues(
                "pairing.provisioner.mode=dynsec",
                "mqtt.broker-urls=" + BROKERS,
                "mqtt.ca-cert=" + CA_BASE64,
                "mqtt.dynsec.admin-username=spottrack-dynsec-admin",
                "mqtt.dynsec.admin-password=admin-pw");
        return overrides.length == 0 ? runnerWithBase : runnerWithBase.withPropertyValues(overrides);
    }

    @Test
    void completeConfigurationActivatesTheDynsecProvisionerAsTheOnlyOne() {
        dynsec().run(context -> {
            assertNull(context.getStartupFailure());
            assertEquals(1, context.getBeansOfType(BrokerCredentialProvisioner.class).size());
            assertInstanceOf(DynamicSecurityBrokerCredentialProvisioner.class, context.getBean(BrokerCredentialProvisioner.class));
        });
    }

    @Test
    void noopModeNeverLoadsTheDynsecConfiguration() {
        runner.withPropertyValues("pairing.provisioner.mode=noop").run(context -> {
            assertInstanceOf(LoggingBrokerCredentialProvisioner.class, context.getBean(BrokerCredentialProvisioner.class));
            assertEquals(0, context.getBeansOfType(DynamicSecurityBrokerCredentialProvisioner.class).size());
        });
    }

    @Test
    void emptyAdminPasswordFailsTheStartup() {
        assertStartupFails(dynsec("mqtt.dynsec.admin-password="), "mqtt.dynsec.admin-password");
    }

    @Test
    void emptyAdminUsernameFailsTheStartup() {
        assertStartupFails(dynsec("mqtt.dynsec.admin-username="), "mqtt.dynsec.admin-username");
    }

    @Test
    void missingCaCertificateFailsTheStartup() {
        assertStartupFails(dynsec("mqtt.ca-cert="), "mqtt.ca-cert is empty");
    }

    @Test
    void invalidCaCertificateFailsTheStartup() {
        assertStartupFails(dynsec("mqtt.ca-cert=garbage"), "mqtt.ca-cert is not a valid certificate");
    }

    @Test
    void emptyBrokerListFailsTheStartup() {
        assertStartupFails(dynsec("mqtt.broker-urls="), "mqtt.broker-urls is empty");
    }

    @Test
    void plainTcpBrokerFailsTheStartup() {
        assertStartupFails(dynsec("mqtt.broker-urls=ssl://10.0.0.1:8883,tcp://10.0.0.2:1883"), "ssl://");
    }

    @Test
    void nonPositiveTimeoutOrAttemptsFailTheStartup() {
        assertStartupFails(dynsec("pairing.dynsec.total-timeout=0s"), "pairing.dynsec.total-timeout");
        assertStartupFails(dynsec("pairing.dynsec.max-attempts=0"), "pairing.dynsec.max-attempts");
    }

    @Test
    void theFailureMessageNeverContainsTheAdminPassword() {
        dynsec("mqtt.broker-urls=").run(context -> {
            var message = context.getStartupFailure().getMessage() + causes(context.getStartupFailure());
            assertFalse(message.contains("admin-pw"));
        });
    }

    private static void assertStartupFails(ApplicationContextRunner runner, String expectedFragment) {
        runner.run(context -> {
            assertNotNull(context.getStartupFailure(), "the context must not start");
            assertTrue(causes(context.getStartupFailure()).contains(expectedFragment), causes(context.getStartupFailure()));
        });
    }

    private static String causes(Throwable failure) {
        var text = new StringBuilder();
        for (var cause = failure; cause != null; cause = cause.getCause()) text.append(cause.getMessage()).append(" | ");
        return text.toString();
    }
}
