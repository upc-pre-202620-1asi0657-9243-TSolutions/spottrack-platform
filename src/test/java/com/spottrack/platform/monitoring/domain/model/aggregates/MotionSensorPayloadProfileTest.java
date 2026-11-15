package com.spottrack.platform.monitoring.domain.model.aggregates;

import com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MotionSensorPayloadProfileTest {

    @Test
    void registrationKeepsTheRequestedProfile() {
        var sensor = new MotionSensor(new RegisterMotionSensorCommand("eq-1", "vendor-x"));

        assertEquals("vendor-x", sensor.getPayloadProfile());
    }

    @Test
    void registrationWithoutProfileLeavesItNull() {
        var sensor = new MotionSensor(new RegisterMotionSensorCommand("eq-1", null));

        assertNull(sensor.getPayloadProfile());
    }

    @Test
    void registrationWithBlankProfileNormalizesToNull() {
        assertNull(new MotionSensor(new RegisterMotionSensorCommand("eq-1", "")).getPayloadProfile());
        assertNull(new MotionSensor(new RegisterMotionSensorCommand("eq-1", "   ")).getPayloadProfile());
    }

    @Test
    void rehydrationKeepsTheStoredProfile() {
        assertEquals("vendor-x", rehydratedWith("vendor-x").getPayloadProfile());
    }

    @Test
    void rehydrationWithMissingOrBlankProfileNormalizesToNull() {
        assertNull(rehydratedWith(null).getPayloadProfile());
        assertNull(rehydratedWith(" ").getPayloadProfile());
    }

    private static MotionSensor rehydratedWith(String payloadProfile) {
        var now = LocalDateTime.now();
        return new MotionSensor(1L, new MotionSensorId("dev-1"), new EquipmentId("eq-1"), now, false, now, null,
                payloadProfile);
    }
}
