package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.interfaces.rest.resources.RegisterMotionSensorResource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MotionSensorPayloadProfileTransformTest {

    @Test
    void commandCarriesTheProfileOfTheRequest() {
        var command = RegisterMotionSensorCommandFromResource
                .toCommandFromResource(new RegisterMotionSensorResource("eq-1", "vendor-x"));

        assertEquals("eq-1", command.equipmentId());
        assertEquals("vendor-x", command.payloadProfile());
    }

    @Test
    void commandWithoutProfileKeepsItNull() {
        var command = RegisterMotionSensorCommandFromResource
                .toCommandFromResource(new RegisterMotionSensorResource("eq-1", null));

        assertNull(command.payloadProfile());
    }

    @Test
    void resourceExposesTheProfileOfTheSensor() {
        var resource = MotionSensorResourceFromEntity.toResourceFromEntity(sensorWith("vendor-x"), null);

        assertEquals("vendor-x", resource.payloadProfile());
        assertEquals("dev-1", resource.motionSensorId());
        assertEquals("eq-1", resource.equipmentId());
    }

    @Test
    void resourceOfASensorWithoutProfileExposesNull() {
        var resource = MotionSensorResourceFromEntity.toResourceFromEntity(sensorWith(null), null);

        assertNull(resource.payloadProfile());
    }

    private static MotionSensor sensorWith(String payloadProfile) {
        var now = LocalDateTime.now();
        return new MotionSensor(1L, new MotionSensorId("dev-1"), new EquipmentId("eq-1"), now, false, now, null,
                payloadProfile);
    }
}
