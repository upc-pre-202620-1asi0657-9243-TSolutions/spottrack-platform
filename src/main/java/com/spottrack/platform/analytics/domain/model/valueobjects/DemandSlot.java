package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record DemandSlot(String timeSlot, Integer estimatedUsers) {
    public DemandSlot {
        if (timeSlot == null || timeSlot.isBlank()) {
            throw new IllegalArgumentException("Time slot cannot be empty");
        }
        if (estimatedUsers == null || estimatedUsers < 0) {
            throw new IllegalArgumentException("Estimated users cannot be negative");
        }
    }
}