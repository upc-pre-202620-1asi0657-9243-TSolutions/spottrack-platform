package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.MotionSensorPersistenceEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MotionSensorPersistenceAssemblerTest {

    @Test
    void roundTripKeepsThePayloadProfile() {
        var sensor = sensorWith("vendor-x");

        var entity = MotionSensorPersistenceAssembler.toPersistenceFromDomain(sensor);
        var restored = MotionSensorPersistenceAssembler.toDomainFromPersistence(entity);

        assertEquals("vendor-x", entity.getPayloadProfile());
        assertEquals("vendor-x", restored.getPayloadProfile());
        assertEquals(sensor.getMotionSensorId(), restored.getMotionSensorId());
        assertEquals(sensor.getEquipmentId(), restored.getEquipmentId());
    }

    @Test
    void roundTripWithoutProfileStaysNull() {
        var entity = MotionSensorPersistenceAssembler.toPersistenceFromDomain(sensorWith(null));
        var restored = MotionSensorPersistenceAssembler.toDomainFromPersistence(entity);

        assertNull(entity.getPayloadProfile());
        assertNull(restored.getPayloadProfile());
    }

    @Test
    void legacyRowsWithoutProfileColumnValueRestoreAsNull() {
        var now = LocalDateTime.now();
        var entity = new MotionSensorPersistenceEntity();
        entity.setMotionSensorId(new MotionSensorId("dev-legacy"));
        entity.setEquipmentId(new EquipmentId("eq-legacy"));
        entity.setRegisteredAt(now);
        entity.setLastStatusChangeAt(now);

        assertNull(MotionSensorPersistenceAssembler.toDomainFromPersistence(entity).getPayloadProfile());
    }

    private static MotionSensor sensorWith(String payloadProfile) {
        var now = LocalDateTime.now();
        return new MotionSensor(1L, new MotionSensorId("dev-1"), new EquipmentId("eq-1"), now, false, now, null,
                payloadProfile);
    }
}
