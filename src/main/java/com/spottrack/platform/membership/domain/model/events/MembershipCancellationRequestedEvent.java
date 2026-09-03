package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;

import java.time.LocalDate;
import java.util.UUID;

public record MembershipCancellationRequestedEvent(
        Long id,
        UUID membershipId,
        Long clientId,
        LocalDate accessUntil
) {
    public static MembershipCancellationRequestedEvent from(Membership membership) {
        return new MembershipCancellationRequestedEvent(
                membership.getId(),
                membership.getMembershipId().uuid(),
                membership.getClientId(),
                membership.getMembershipPeriod().endDate()
        );
    }
}
