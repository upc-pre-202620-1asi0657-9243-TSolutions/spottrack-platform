package com.spottrack.platform.membership.domain.model.valueobjects;

import java.util.UUID;

public record BranchAccessId(UUID uuid) {
    public BranchAccessId() { this(UUID.randomUUID()); }

    public BranchAccessId {
        if (uuid == null) {
            throw new IllegalArgumentException("membership.error.branchAccessId.notNull");
        }
    }
}
