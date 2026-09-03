package com.spottrack.platform.membership.interfaces.rest.transform;

import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;
import com.spottrack.platform.membership.interfaces.rest.resources.BranchAccessResource;

public class BranchAccessResourceFromEntityAssembler {

    public static BranchAccessResource toResourceFromEntity(BranchAccess branchAccess) {
        return new BranchAccessResource(
                branchAccess.getId(),
                branchAccess.getBranchAccessId().uuid(),
                branchAccess.getMembershipId(),
                branchAccess.getBranchId(),
                branchAccess.getStatus().name()
        );
    }
}
