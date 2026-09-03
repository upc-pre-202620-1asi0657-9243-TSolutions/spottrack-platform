package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;

import java.time.LocalDate;
import java.util.UUID;

public record MembershipDowngradeRequestedEvent(
        Long id,
        UUID membershipId,
        Long clientId,
        MembershipTier currentTier,
        MembershipTier pendingDowngradeTier,
        LocalDate effectiveDate
) {
    public static MembershipDowngradeRequestedEvent from(Membership membership) {
        return new MembershipDowngradeRequestedEvent(
                membership.getId(),
                membership.getMembershipId().uuid(),
                membership.getClientId(),
                membership.getMembershipTier(),
                membership.getPendingDowngradeTier(),
                membership.getMembershipPeriod().endDate()
        );
    }
}
