package com.spottrack.platform.gym.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record BranchId(String uuid) {
    private static final String NOT_BLANK = "branch.error.branchId.notBlank";

    public BranchId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK);
        }
    }
}
