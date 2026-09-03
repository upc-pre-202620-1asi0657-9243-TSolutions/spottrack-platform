package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record MaintenanceCost(Double amount, String currency) {
    public MaintenanceCost {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Maintenance cost amount cannot be negative");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency type cannot be empty");
        }
        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency must be a 3-letter ISO code");
        }
    }
}