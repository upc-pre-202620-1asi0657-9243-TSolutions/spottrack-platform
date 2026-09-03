package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;

import java.util.UUID;

public record MembershipExpiredEvent(
        Long id,
        UUID membershipId,
        Long clientId
) {
    public static MembershipExpiredEvent from(Membership membership) {
        return new MembershipExpiredEvent(
                membership.getId(),
                membership.getMembershipId().uuid(),
                membership.getClientId()
        );
    }
}
