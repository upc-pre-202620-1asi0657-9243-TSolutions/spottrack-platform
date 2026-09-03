package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record UsageTime(Integer minutesActive) {
    public UsageTime {
        if (minutesActive == null || minutesActive < 0) {
            throw new IllegalArgumentException("Usage time in minutes cannot be negative");
        }
    }
}