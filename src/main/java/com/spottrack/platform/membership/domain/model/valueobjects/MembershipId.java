package com.spottrack.platform.membership.domain.model.valueobjects;

import java.util.UUID;

public record MembershipId(UUID uuid) {
    public MembershipId() { this(UUID.randomUUID()); }

    public MembershipId {
        if (uuid == null) {
            throw new IllegalArgumentException("membership.error.membershipId.notNull");
        }
    }
}
