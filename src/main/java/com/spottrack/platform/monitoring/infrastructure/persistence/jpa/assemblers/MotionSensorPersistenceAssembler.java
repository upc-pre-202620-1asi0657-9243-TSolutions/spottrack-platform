package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.MotionSensorPersistenceEntity;

public class MotionSensorPersistenceAssembler {
    public static MotionSensor toDomainFromPersistence(MotionSensorPersistenceEntity entity) {
        return new MotionSensor(
                entity.getId(),
                entity.getMotionSensorId(),
                entity.getEquipmentId(),
                entity.getRegisteredAt(),
                entity.isOnline(),
                entity.getLastStatusChangeAt()
        );
    }

    public static MotionSensorPersistenceEntity toPersistenceFromDomain(MotionSensor domain) {
        var entity = new MotionSensorPersistenceEntity();
        entity.setId(domain.getId());
        entity.setMotionSensorId(domain.getMotionSensorId());
        entity.setEquipmentId(domain.getEquipmentId());
        entity.setRegisteredAt(domain.getRegisteredAt());
        entity.setOnline(domain.isOnline());
        entity.setLastStatusChangeAt(domain.getLastStatusChangeAt());
        return entity;
    }
}
