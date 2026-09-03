package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceLog;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenanceLogPersistenceEntity;

public class MaintenanceLogPersistenceAssembler {

    public static MaintenanceLog toDomainFromPersistence(MaintenanceLogPersistenceEntity entity) {
        return new MaintenanceLog(
                entity.getLogId(),
                entity.getTicketId(),
                entity.getMaintenanceId(),
                entity.getNotes(),
                entity.getCost(),
                entity.getCompletedAt()
        );
    }

    public static MaintenanceLogPersistenceEntity toPersistenceFromDomain(MaintenanceLog log) {
        var entity = new MaintenanceLogPersistenceEntity();
        entity.setLogId(log.getLogId().uuid());
        entity.setTicketId(log.getTicketId());
        entity.setMaintenanceId(log.getMaintenanceId());
        entity.setNotes(log.getNotes());
        entity.setCost(log.getCost());
        entity.setCompletedAt(log.getCompletedAt());
        return entity;
    }
}
