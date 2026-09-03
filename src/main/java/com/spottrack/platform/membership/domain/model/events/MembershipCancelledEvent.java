package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;

import java.util.UUID;

public record MembershipCancelledEvent(
        Long id,
        UUID membershipId,
        Long clientId
) {
    public static MembershipCancelledEvent from(Membership membership) {
        return new MembershipCancelledEvent(
                membership.getId(),
                membership.getMembershipId().uuid(),
                membership.getClientId()
        );
    }
}
