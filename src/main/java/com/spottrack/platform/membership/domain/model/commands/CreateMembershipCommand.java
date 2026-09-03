package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;

import java.time.LocalDate;

public record CreateMembershipCommand(
        Long clientId,
        MembershipTier membershipTier,
        Money price,
        LocalDate startDate,
        LocalDate endDate
) {
    public CreateMembershipCommand {
        if (clientId == null) throw new IllegalArgumentException("membership.error.clientId.notNull");
        if (membershipTier == null) throw new IllegalArgumentException("membership.error.membershipTier.notNull");
        if (price == null) throw new IllegalArgumentException("membership.error.price.notNull");
        if (startDate == null) throw new IllegalArgumentException("membership.error.startDate.notNull");
        if (endDate == null) throw new IllegalArgumentException("membership.error.endDate.notNull");
    }
}
