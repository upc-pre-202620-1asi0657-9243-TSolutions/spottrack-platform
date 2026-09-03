package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record FinancialMetric(Double expectedRoiPercentage, Double projectedRevenue) {
    public FinancialMetric {
        if (expectedRoiPercentage == null) {
            throw new IllegalArgumentException("Expected ROI percentage cannot be null");
        }
        if (expectedRoiPercentage < -100) {
            throw new IllegalArgumentException("Expected ROI percentage cannot be below -100%");
        }
        if (projectedRevenue == null || projectedRevenue < 0) {
            throw new IllegalArgumentException("Projected revenue cannot be negative");
        }
    }
}