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
