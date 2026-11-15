package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.SensorPairingPersistenceEntity;

public class SensorPairingPersistenceAssembler {
    public static SensorPairing toDomainFromPersistence(SensorPairingPersistenceEntity entity) {
        return new SensorPairing(
                entity.getId(),
                entity.getMotionSensorId(),
                entity.getSecretHash(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.getStatus(),
                entity.getUsedAt()
        );
    }

    public static SensorPairingPersistenceEntity toPersistenceFromDomain(SensorPairing domain) {
        var entity = new SensorPairingPersistenceEntity();
        entity.setId(domain.getId());
        entity.setMotionSensorId(domain.getMotionSensorId());
        entity.setSecretHash(domain.getSecretHash());
        entity.setIssuedAt(domain.getIssuedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setStatus(domain.getStatus());
        entity.setUsedAt(domain.getUsedAt());
        return entity;
    }
}
