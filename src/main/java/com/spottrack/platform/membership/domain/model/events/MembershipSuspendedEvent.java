package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;

import java.util.UUID;

public record MembershipSuspendedEvent(
        Long id,
        UUID membershipId,
        Long clientId
) {
    public static MembershipSuspendedEvent from(Membership membership) {
        return new MembershipSuspendedEvent(
                membership.getId(),
                membership.getMembershipId().uuid(),
                membership.getClientId()
        );
    }
}
