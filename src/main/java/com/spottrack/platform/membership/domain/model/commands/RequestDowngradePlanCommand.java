package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;

public record RequestDowngradePlanCommand(MembershipId membershipId, MembershipTier newTier) {
    public RequestDowngradePlanCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
        if (newTier == null) throw new IllegalArgumentException("membership.error.membershipTier.notNull");
    }
}
