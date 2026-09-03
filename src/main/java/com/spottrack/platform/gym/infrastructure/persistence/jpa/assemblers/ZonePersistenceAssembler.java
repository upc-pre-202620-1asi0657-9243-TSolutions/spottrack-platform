package com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.gym.domain.model.entities.Zone;
import com.spottrack.platform.gym.domain.model.valueobjects.BranchId;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.ZonePersistenceEntity;

public class ZonePersistenceAssembler {
    private ZonePersistenceAssembler() {

    }

    public static Zone toDomainFromPersistence(ZonePersistenceEntity entity) {
        var branchId = new BranchId(entity.getBranchId());
        return new Zone(entity.getZoneId(), entity.getName(), entity.getMaximumOccupancy(), branchId);
    }

    public static ZonePersistenceEntity toPersistenceFromDomain(Zone entity) {
        var zonePersistenceEntity = new ZonePersistenceEntity();
        zonePersistenceEntity.setZoneId(entity.getId().uuid());
        zonePersistenceEntity.setName(entity.getName());
        zonePersistenceEntity.setMaximumOccupancy(entity.getMaximumOccupancy());
        zonePersistenceEntity.setBranchId(entity.getBranchId().uuid());
        zonePersistenceEntity.setZoneId(entity.getId().uuid());
        return zonePersistenceEntity;
    }

}
