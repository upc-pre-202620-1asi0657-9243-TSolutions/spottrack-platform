package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.shared.application.result.ApplicationError;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RejectedSensorSignalRegistryTest {

    private final RejectedSensorSignalRegistry registry = new RejectedSensorSignalRegistry();

    @Test
    void startsWithNoRejections() {
        var stats = registry.stats();

        assertEquals(0, stats.total());
        assertEquals(Map.of(), stats.byProfile());
        assertNull(stats.lastRejected());
    }

    @Test
    void rejectionWithoutProfileCountsUnderTheDefaultProfile() {
        registry.record("dev-1", null, invalidPayload());

        var stats = registry.stats();
        assertEquals(1, stats.total());
        assertEquals(Map.of("default", 1L), stats.byProfile());
    }

    @Test
    void countsRejectionsPerProfile() {
        registry.record("dev-1", "default", invalidPayload());
        registry.record("dev-2", "vendor-x", invalidPayload());
        registry.record("dev-3", "vendor-x", invalidPayload());

        var stats = registry.stats();
        assertEquals(3, stats.total());
        assertEquals(Map.of("default", 1L, "vendor-x", 2L), stats.byProfile());
    }

    @Test
    void keepsTheDeviceProfileAndReasonOfTheLastRejection() {
        registry.record("dev-1", "default", invalidPayload());
        registry.record("dev-2", "vendor-x", invalidPayload());

        var last = registry.stats().lastRejected();
        assertEquals("dev-2", last.deviceId());
        assertEquals("vendor-x", last.payloadProfile());
        assertEquals("Validation failed: sensorPayload: Unexpected token", last.reason());
        assertNotNull(last.rejectedAt());
    }

    private static ApplicationError invalidPayload() {
        return ApplicationError.validationError("sensorPayload", "Unexpected token");
    }
}
