package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;

public record InitiateResubscriptionPaymentCommand(
        Long userId,
        MembershipTier membershipTier,
        Money amount
) {}
