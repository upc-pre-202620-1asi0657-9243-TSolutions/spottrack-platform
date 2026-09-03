package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;

public record UpgradeMembershipPlanCommand(MembershipId membershipId, MembershipTier newMembershipTier) {
    public UpgradeMembershipPlanCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
        if (newMembershipTier == null) throw new IllegalArgumentException("membership.error.membershipTier.notNull");
    }
}
