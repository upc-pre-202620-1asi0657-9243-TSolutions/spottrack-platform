package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.monitoring.domain.model.aggregates.CameraSensor;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.CameraSensorPersistenceEntity;

public class CameraSensorPersistenceAssembler {
    public static CameraSensor toDomainFromPersistence(CameraSensorPersistenceEntity entity) {
        return new CameraSensor(entity.getId(), entity.getCameraSensorId(), entity.getEquipmentId(), entity.getRegisteredAt());
    }

    public static CameraSensorPersistenceEntity toPersistenceFromDomain(CameraSensor domain) {
        var entity = new CameraSensorPersistenceEntity();
        entity.setId(domain.getId());
        entity.setCameraSensorId(domain.getCameraSensorId());
        entity.setEquipmentId(domain.getEquipmentId());
        entity.setRegisteredAt(domain.getRegisteredAt());
        return entity;
    }
}
