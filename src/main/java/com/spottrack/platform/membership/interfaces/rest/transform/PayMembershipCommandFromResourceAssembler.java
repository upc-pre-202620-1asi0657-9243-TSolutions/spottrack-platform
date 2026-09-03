package com.spottrack.platform.membership.interfaces.rest.transform;

import com.spottrack.platform.membership.domain.model.commands.PayMembershipCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.interfaces.rest.resources.PayMembershipResource;

public class PayMembershipCommandFromResourceAssembler {

    public static PayMembershipCommand toCommandFromResource(PayMembershipResource resource) {
        var tier = MembershipTier.valueOf(resource.membershipTier());
        return new PayMembershipCommand(
                resource.userId(),
                tier,
                tier.toMoney()
        );
    }
}
