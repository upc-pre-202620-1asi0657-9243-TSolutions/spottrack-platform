package com.spottrack.platform.notification.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.infrastructure.persistence.jpa.entities.EquipmentWatchPersistenceEntity;

public final class EquipmentWatchPersistenceAssembler {

    private EquipmentWatchPersistenceAssembler() {}

    public static EquipmentWatch toDomainFromPersistence(EquipmentWatchPersistenceEntity entity) {
        return new EquipmentWatch(
                entity.getId(),
                entity.getClientId(),
                entity.getEquipmentId(),
                entity.getOrigin(),
                entity.getStatus(),
                entity.getNotifiedAt(),
                entity.getCreatedAt()
        );
    }

    public static EquipmentWatchPersistenceEntity toPersistenceFromDomain(EquipmentWatch watch) {
        var entity = new EquipmentWatchPersistenceEntity();
        entity.setId(watch.getId());
        entity.setCreatedAt(watch.getCreatedAt());
        entity.setClientId(watch.getClientId());
        entity.setEquipmentId(watch.getEquipmentId());
        entity.setOrigin(watch.getOrigin());
        entity.setStatus(watch.getStatus());
        entity.setNotifiedAt(watch.getNotifiedAt());
        return entity;
    }
}
