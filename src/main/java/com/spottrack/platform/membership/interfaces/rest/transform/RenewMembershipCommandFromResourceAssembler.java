package com.spottrack.platform.membership.interfaces.rest.transform;

import com.spottrack.platform.membership.domain.model.commands.RenewMembershipCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;
import com.spottrack.platform.membership.interfaces.rest.resources.RenewMembershipResource;

import java.util.UUID;

public class RenewMembershipCommandFromResourceAssembler {

    public static RenewMembershipCommand toCommand(UUID membershipId, RenewMembershipResource resource) {
        return new RenewMembershipCommand(
                new MembershipId(membershipId),
                resource.startDate(),
                resource.endDate()
        );
    }
}
