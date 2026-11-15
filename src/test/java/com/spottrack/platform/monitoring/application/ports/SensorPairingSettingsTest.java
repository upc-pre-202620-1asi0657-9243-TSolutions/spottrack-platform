package com.spottrack.platform.monitoring.application.ports;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorPairingSettingsTest {

    private static final String CA = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
    private static final Duration TTL = Duration.ofMinutes(15);

    private static String messageOf(SensorPairingSettings settings) {
        return assertThrows(IllegalStateException.class, settings::requireConfigured).getMessage();
    }

    @Test
    void splitsHostAndPortOfThePublicEndpoint() {
        var settings = new SensorPairingSettings(TTL, " 34.123.45.67:8883 ", CA);

        assertEquals("34.123.45.67", settings.host());
        assertEquals(8883, settings.port());
        assertDoesNotThrow(settings::requireConfigured);
    }

    @Test
    void normalizesTheOpensslColonFormatOfTheFingerprint() {
        var withColons = CA.toUpperCase().replaceAll("(..)(?!$)", "$1:");

        assertEquals(CA, new SensorPairingSettings(TTL, "h:8883", withColons).normalizedCaSha256());
    }

    @Test
    void emptyEndpointAndFingerprintAreReportedByPropertyName() {
        assertTrue(messageOf(new SensorPairingSettings(TTL, "", CA)).contains("mqtt.public-endpoint is not configured"));
        assertTrue(messageOf(new SensorPairingSettings(TTL, null, CA)).contains("mqtt.public-endpoint is not configured"));
        assertTrue(messageOf(new SensorPairingSettings(TTL, "h:8883", "")).contains("mqtt.ca-sha256 is not configured"));
    }

    @Test
    void rejectsAnEndpointWithoutAValidPort() {
        for (var endpoint : new String[]{"host", "host:", ":8883", "host:abc", "host:0", "host:70000"}) {
            assertTrue(messageOf(new SensorPairingSettings(TTL, endpoint, CA)).contains("host:port"), endpoint);
        }
    }

    @Test
    void rejectsAFingerprintThatIsNotSha256Hex() {
        assertTrue(messageOf(new SensorPairingSettings(TTL, "h:8883", "abc")).contains("64 hexadecimal"));
        assertTrue(messageOf(new SensorPairingSettings(TTL, "h:8883", "z".repeat(64))).contains("64 hexadecimal"));
    }

    @Test
    void rejectsANonPositiveTtl() {
        assertTrue(messageOf(new SensorPairingSettings(Duration.ZERO, "h:8883", CA)).contains("pairing.ttl"));
        assertTrue(messageOf(new SensorPairingSettings(null, "h:8883", CA)).contains("pairing.ttl"));
    }
}
