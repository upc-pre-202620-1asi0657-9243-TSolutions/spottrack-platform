package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;

import java.util.UUID;

public record BranchAccessDeniedEvent(
        Long id,
        UUID branchAccessId,
        Long membershipId,
        Long branchId
) {
    public static BranchAccessDeniedEvent from(BranchAccess branchAccess) {
        return new BranchAccessDeniedEvent(
                branchAccess.getId(),
                branchAccess.getBranchAccessId().uuid(),
                branchAccess.getMembershipId(),
                branchAccess.getBranchId()
        );
    }
}
