package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Downtime(Integer minutesInactive, String reason) {
    public Downtime {
        if (minutesInactive == null || minutesInactive < 0) {
            throw new IllegalArgumentException("Downtime minutes cannot be negative");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Downtime reason cannot be empty");
        }
    }
}