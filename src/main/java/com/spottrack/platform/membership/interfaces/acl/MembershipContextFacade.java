package com.spottrack.platform.membership.interfaces.acl;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;

import java.util.Optional;

public interface MembershipContextFacade {

    /**
     * Returns "ACTIVE", "SUSPENDED", or "INACTIVE" (includes no membership, CANCELLED, EXPIRED).
     */
    String fetchMembershipAccessStatus(Long clientId);

    /**
     * Returns the tier of the current ACTIVE membership, or empty if none.
     */
    Optional<MembershipTier> fetchActiveMembershipTier(Long clientId);
}
