package com.spottrack.platform.membership.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;
import com.spottrack.platform.membership.domain.model.valueobjects.BranchAccessId;
import com.spottrack.platform.membership.infrastructure.persistence.jpa.entities.BranchAccessPersistenceEntity;

import java.util.UUID;

public final class BranchAccessPersistenceAssembler {

    private BranchAccessPersistenceAssembler() {}

    public static BranchAccess toDomainFromPersistence(BranchAccessPersistenceEntity entity) {
        return new BranchAccess(
                entity.getId(),
                new BranchAccessId(UUID.fromString(entity.getBranchAccessId())),
                entity.getMembershipId(),
                entity.getBranchId(),
                entity.getStatus()
        );
    }

    public static BranchAccessPersistenceEntity toPersistenceFromDomain(BranchAccess branchAccess) {
        var entity = new BranchAccessPersistenceEntity();
        entity.setId(branchAccess.getId());
        entity.setBranchAccessId(branchAccess.getBranchAccessId().uuid().toString());
        entity.setMembershipId(branchAccess.getMembershipId());
        entity.setBranchId(branchAccess.getBranchId());
        entity.setStatus(branchAccess.getStatus());
        return entity;
    }
}
