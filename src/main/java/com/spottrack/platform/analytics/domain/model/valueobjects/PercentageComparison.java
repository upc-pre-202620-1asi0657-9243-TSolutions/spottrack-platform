package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record PercentageComparison(Double percentageChange) {
    public PercentageComparison {
        if (percentageChange == null) {
            throw new IllegalArgumentException("Percentage change cannot be null");
        }
    }
}