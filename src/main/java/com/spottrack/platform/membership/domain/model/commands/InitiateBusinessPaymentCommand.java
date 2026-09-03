package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;

import java.util.UUID;

public record InitiateBusinessPaymentCommand(
        UUID pendingRegistrationId,
        MembershipTier membershipTier,
        Money amount
) {
    public InitiateBusinessPaymentCommand {
        if (pendingRegistrationId == null) throw new IllegalArgumentException("pendingRegistrationId must not be null");
        if (membershipTier == null) throw new IllegalArgumentException("membershipTier must not be null");
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
    }
}
