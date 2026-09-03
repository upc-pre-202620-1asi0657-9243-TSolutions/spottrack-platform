package com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;

public class BranchPersistenceAssembler {
    private BranchPersistenceAssembler() {

    }

    public static Branch toDomainFromPersistence(BranchPersistenceEntity entity){
        return new Branch(entity.getBranchId(), entity.getGymId(), entity.getName(), entity.getAddress());
    }

    public static BranchPersistenceEntity toPersistenceFromDomain(Branch entity){
        var branchEntity = new BranchPersistenceEntity();
        branchEntity.setBranchId(entity.getId().uuid());
        branchEntity.setGymId(entity.getGymId());
        branchEntity.setName(entity.getName());
        branchEntity.setAddress(entity.getAddress());
        return branchEntity;
    }
}
