package com.spottrack.platform.membership.interfaces.rest.transform;

import com.spottrack.platform.membership.domain.model.commands.UpgradeMembershipPlanCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.interfaces.rest.resources.UpgradeMembershipPlanResource;

import java.util.UUID;

public class UpgradeMembershipPlanCommandFromResourceAssembler {

    public static UpgradeMembershipPlanCommand toCommand(UUID membershipId, UpgradeMembershipPlanResource resource) {
        return new UpgradeMembershipPlanCommand(
                new MembershipId(membershipId),
                MembershipTier.valueOf(resource.newMembershipTier())
        );
    }
}
