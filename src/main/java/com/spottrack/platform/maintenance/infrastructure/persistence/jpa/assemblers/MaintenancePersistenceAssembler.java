package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenancePersistenceEntity;

public class MaintenancePersistenceAssembler {

    public static Maintenance toDomainFromPersistence(MaintenancePersistenceEntity entity) {
        return new Maintenance(
                entity.getMaintenanceId(),
                entity.getEquipmentId(),
                entity.getRequestedBy(),
                entity.getDescription(),
                entity.getStatus());
    }

    public static MaintenancePersistenceEntity toPersistenceFromDomain(Maintenance entity) {
        var persistenceEntity = new MaintenancePersistenceEntity();
        persistenceEntity.setMaintenanceId(entity.getId().uuid());
        persistenceEntity.setEquipmentId(entity.getEquipmentId().uuid());
        persistenceEntity.setRequestedBy(entity.getRequestedBy());
        persistenceEntity.setDescription(entity.getDescription());
        persistenceEntity.setStatus(entity.getStatus());
        return persistenceEntity;
    }
}
