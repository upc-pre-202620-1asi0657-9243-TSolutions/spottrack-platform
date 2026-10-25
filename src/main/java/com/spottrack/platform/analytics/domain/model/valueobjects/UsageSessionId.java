package com.spottrack.platform.analytics.domain.model.valueobjects;

public record UsageSessionId(String value) {
    public UsageSessionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UsageSessionId must not be blank");
        }
    }
}
