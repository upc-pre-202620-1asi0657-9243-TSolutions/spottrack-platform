package com.spottrack.platform.monitoring.infrastructure.pairing;

import com.spottrack.platform.monitoring.application.ports.BrokerCredentialProvisioner;
import com.spottrack.platform.monitoring.application.ports.SensorPairingSettings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SensorPairingConfigurationTest {

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

    private static final String CA_BASE64 = java.util.Base64.getEncoder()
            .encodeToString(CA_PEM.getBytes(java.nio.charset.StandardCharsets.UTF_8));

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(SensorPairingConfiguration.class, LoggingBrokerCredentialProvisioner.class);

    @Test
    void bindsTheTtlAndTheMqttPropertiesIntoTheSettings() {
        runner.withPropertyValues("pairing.ttl=10m", "mqtt.public-endpoint=mqtt.example.com:8883", "mqtt.ca-sha256=" + "a".repeat(64))
                .run(context -> {
                    var settings = context.getBean(SensorPairingSettings.class);
                    assertEquals(Duration.ofMinutes(10), settings.ttl());
                    assertEquals("mqtt.example.com", settings.host());
                    assertEquals(8883, settings.port());
                    assertEquals("a".repeat(64), settings.normalizedCaSha256());
                });
    }

    @Test
    void caSha256IsDerivedFromTheCaCertificateWhenNotSet() {
        runner.withPropertyValues("mqtt.ca-sha256=", "mqtt.ca-cert=" + CA_BASE64).run(context ->
                assertEquals("7186a185daa69dca655bebf6fcc910d69242674e9e816aab937e8d48c32e3318",
                        context.getBean(SensorPairingSettings.class).normalizedCaSha256()));
    }

    @Test
    void explicitCaSha256WinsOverTheCaCertificate() {
        runner.withPropertyValues("mqtt.ca-sha256=" + "b".repeat(64), "mqtt.ca-cert=" + CA_BASE64).run(context ->
                assertEquals("b".repeat(64), context.getBean(SensorPairingSettings.class).normalizedCaSha256()));
    }

    @Test
    void unreadableCaCertificateLeavesTheShaEmptyInsteadOfFailingTheStartup() {
        runner.withPropertyValues("mqtt.ca-sha256=", "mqtt.ca-cert=garbage").run(context -> {
            var settings = context.getBean(SensorPairingSettings.class);
            assertEquals("", settings.normalizedCaSha256());
            assertThrows(IllegalStateException.class, settings::requireConfigured);
        });
    }

    @Test
    void ttlDefaultsToFifteenMinutes() {
        runner.run(context -> assertEquals(Duration.ofMinutes(15), context.getBean(SensorPairingSettings.class).ttl()));
    }

    @Test
    void emptyProdPropertiesStartTheContextButRefuseToIssue() {
        runner.withPropertyValues("mqtt.public-endpoint=", "mqtt.ca-sha256=").run(context -> {
            var settings = context.getBean(SensorPairingSettings.class);
            var error = assertThrows(IllegalStateException.class, settings::requireConfigured);
            assertTrue(error.getMessage().contains("mqtt.public-endpoint"));
        });
    }

    @Test
    void noopProvisionerIsTheDefaultAndCanBeSelectedExplicitly() {
        runner.run(context -> assertInstanceOf(LoggingBrokerCredentialProvisioner.class, context.getBean(BrokerCredentialProvisioner.class)));
        runner.withPropertyValues("pairing.provisioner.mode=noop")
                .run(context -> assertEquals(1, context.getBeansOfType(BrokerCredentialProvisioner.class).size()));
    }

    @Test
    void anotherModeDoesNotActivateTheNoopProvisioner() {
        runner.withPropertyValues("pairing.provisioner.mode=dynsec")
                .run(context -> assertEquals(0, context.getBeansOfType(BrokerCredentialProvisioner.class).size()));
    }
}
