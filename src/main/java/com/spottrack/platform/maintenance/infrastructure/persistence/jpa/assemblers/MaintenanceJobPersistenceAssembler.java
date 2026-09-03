package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceJob;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenanceJobPersistenceEntity;

public class MaintenanceJobPersistenceAssembler {

    public static MaintenanceJob toDomainFromPersistence(MaintenanceJobPersistenceEntity entity) {
        return new MaintenanceJob(
                entity.getJobId(),
                entity.getMaintenanceId(),
                entity.getTechnicianId(),
                entity.isAccepted()
        );
    }

    public static MaintenanceJobPersistenceEntity toPersistenceFromDomain(MaintenanceJob job) {
        var entity = new MaintenanceJobPersistenceEntity();
        entity.setJobId(job.getJobId().uuid());
        entity.setMaintenanceId(job.getMaintenanceId());
        entity.setTechnicianId(job.getTechnicianId());
        entity.setAccepted(job.isAccepted());
        return entity;
    }
}
