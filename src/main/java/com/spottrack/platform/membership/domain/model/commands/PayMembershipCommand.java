package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;

public record PayMembershipCommand(
        Long userId,
        MembershipTier membershipTier,
        Money amount
) {
    public PayMembershipCommand {
        if (userId == null) throw new IllegalArgumentException("membership.error.userId.notNull");
        if (membershipTier == null) throw new IllegalArgumentException("membership.error.membershipTier.notNull");
        if (amount == null) throw new IllegalArgumentException("membership.error.amount.notNull");
    }
}
