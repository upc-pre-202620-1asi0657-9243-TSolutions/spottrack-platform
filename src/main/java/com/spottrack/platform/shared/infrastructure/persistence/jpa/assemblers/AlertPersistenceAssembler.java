package com.spottrack.platform.shared.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;

public final class AlertPersistenceAssembler {

    private AlertPersistenceAssembler() {}

    public static Alert toDomainFromPersistence(AlertPersistenceEntity entity) {
        return new Alert(
                entity.getId(),
                entity.getAdminUserId(),
                entity.getEquipmentId(),
                entity.getSeverity(),
                entity.getMessage(),
                entity.isResolved(),
                entity.getCreatedAt()
        );
    }

    public static AlertPersistenceEntity toPersistenceFromDomain(Alert alert) {
        var entity = new AlertPersistenceEntity();
        entity.setId(alert.getId());
        entity.setCreatedAt(alert.getCreatedAt());
        entity.setAdminUserId(alert.getAdminUserId());
        entity.setEquipmentId(alert.getEquipmentId());
        entity.setSeverity(alert.getSeverity());
        entity.setMessage(alert.getMessage());
        entity.setResolved(alert.isResolved());
        return entity;
    }
}
