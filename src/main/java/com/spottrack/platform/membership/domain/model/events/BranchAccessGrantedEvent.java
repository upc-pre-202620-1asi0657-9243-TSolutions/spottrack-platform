package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;

import java.util.UUID;

public record BranchAccessGrantedEvent(
        Long id,
        UUID branchAccessId,
        Long membershipId,
        Long branchId
) {
    public static BranchAccessGrantedEvent from(BranchAccess branchAccess) {
        return new BranchAccessGrantedEvent(
                branchAccess.getId(),
                branchAccess.getBranchAccessId().uuid(),
                branchAccess.getMembershipId(),
                branchAccess.getBranchId()
        );
    }
}
