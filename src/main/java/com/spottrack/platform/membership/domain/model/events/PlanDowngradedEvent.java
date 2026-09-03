package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;

import java.time.LocalDate;
import java.util.UUID;

public record PlanDowngradedEvent(
        Long id,
        UUID membershipId,
        Long clientId,
        MembershipTier newTier,
        LocalDate newPeriodStart,
        LocalDate newPeriodEnd
) {
    public static PlanDowngradedEvent from(Membership membership) {
        return new PlanDowngradedEvent(
                membership.getId(),
                membership.getMembershipId().uuid(),
                membership.getClientId(),
                membership.getMembershipTier(),
                membership.getMembershipPeriod().startDate(),
                membership.getMembershipPeriod().endDate()
        );
    }
}
