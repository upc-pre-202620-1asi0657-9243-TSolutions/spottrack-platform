package com.spottrack.platform.membership.interfaces.rest.transform;

import com.spottrack.platform.membership.domain.model.commands.GrantBranchAccessCommand;
import com.spottrack.platform.membership.interfaces.rest.resources.GrantBranchAccessResource;

public class GrantBranchAccessCommandFromResourceAssembler {

    public static GrantBranchAccessCommand toCommand(GrantBranchAccessResource resource) {
        return new GrantBranchAccessCommand(
                resource.membershipId(),
                resource.branchId()
        );
    }
}
