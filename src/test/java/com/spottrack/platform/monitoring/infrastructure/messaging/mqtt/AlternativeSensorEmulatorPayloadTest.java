package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.CompactSensorPayloadTranslator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What the alternative emulator publishes is exactly what the compact-emulator profile reads. */
class AlternativeSensorEmulatorPayloadTest {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final CompactSensorPayloadTranslator compact = new CompactSensorPayloadTranslator(mapper);

    @Test
    void motionPayloadUsesTheCompactKeysOnly() {
        var json = mapper.readTree(AlternativeSensorEmulator.payload("qa-device", "mv", 1.5));

        assertEquals("qa-device", json.get("dev").asString());
        assertEquals("mv", json.get("sig").asString());
        assertEquals(1.5, json.get("dur").asDouble());
        assertTrue(json.has("ts"));
        assertEquals(4, json.size());
    }

    @Test
    void motionPayloadIsReadByTheCompactTranslatorWithSecondsConvertedToMilliseconds() {
        var signal = compact.translate(AlternativeSensorEmulator.payload("qa-device", "mv", 1.5)).toOptional().orElseThrow();

        assertEquals("qa-device", signal.deviceId());
        assertEquals("MOTION", signal.signalType());
        assertEquals(1500L, signal.durationMs());
        Instant.parse(signal.timestamp());
    }

    @Test
    void heartbeatPayloadIsReadAsAHeartbeat() {
        var signal = compact.translate(AlternativeSensorEmulator.payload("qa-device", "hb", 0)).toOptional().orElseThrow();

        assertEquals("qa-device", signal.deviceId());
        assertEquals("HEARTBEAT", signal.signalType());
        assertEquals(0L, signal.durationMs());
    }
}
