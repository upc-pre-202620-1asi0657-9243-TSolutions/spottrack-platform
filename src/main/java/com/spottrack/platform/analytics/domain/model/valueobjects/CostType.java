package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record CostType(String value) {
    public CostType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Cost type description cannot be empty");
        }
    }
}