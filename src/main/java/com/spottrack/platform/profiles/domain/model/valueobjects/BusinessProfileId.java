package com.spottrack.platform.profiles.domain.model.valueobjects;

public record BusinessProfileId(Long businessProfileId) {
    public BusinessProfileId {
        if (businessProfileId == null || businessProfileId < 1)
            throw new IllegalArgumentException("Business profile id cannot be null or less than 1");
    }
}
