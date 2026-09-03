package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;

import java.util.UUID;

public record PlanUpgradedEvent(
        Long id,
        UUID membershipId,
        Long clientId,
        MembershipTier newTier
) {
    public static PlanUpgradedEvent from(Membership membership) {
        return new PlanUpgradedEvent(
                membership.getId(),
                membership.getMembershipId().uuid(),
                membership.getClientId(),
                membership.getMembershipTier()
        );
    }
}
