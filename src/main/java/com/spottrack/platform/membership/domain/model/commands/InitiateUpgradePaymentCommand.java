package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;

import java.util.UUID;

public record InitiateUpgradePaymentCommand(UUID membershipId, MembershipTier newMembershipTier, Money amount) {
    public InitiateUpgradePaymentCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
        if (newMembershipTier == null) throw new IllegalArgumentException("membership.error.membershipTier.notNull");
        if (amount == null) throw new IllegalArgumentException("membership.error.amount.notNull");
    }
}
