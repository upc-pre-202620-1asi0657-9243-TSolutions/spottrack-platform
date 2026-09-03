package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.TechnicianPersistenceEntity;

public class TechnicianPersistenceAssembler {

    public static Technician toDomainFromPersistence(TechnicianPersistenceEntity entity) {
        return new Technician(entity.getTechnicianId(), entity.getName());
    }

    public static TechnicianPersistenceEntity toPersistenceFromDomain(Technician technician) {
        var entity = new TechnicianPersistenceEntity();
        entity.setTechnicianId(technician.getTechnicianId().uuid());
        entity.setName(technician.getName());
        return entity;
    }
}
