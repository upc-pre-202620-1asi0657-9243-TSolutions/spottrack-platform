package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;

import java.util.UUID;

public record MembershipCancellationUndoneEvent(
        Long id,
        UUID membershipId,
        Long clientId
) {
    public static MembershipCancellationUndoneEvent from(Membership membership) {
        return new MembershipCancellationUndoneEvent(
                membership.getId(),
                membership.getMembershipId().uuid(),
                membership.getClientId()
        );
    }
}
