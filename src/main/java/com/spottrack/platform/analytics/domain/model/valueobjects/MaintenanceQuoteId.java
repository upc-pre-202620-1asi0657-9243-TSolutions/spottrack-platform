package com.spottrack.platform.analytics.domain.model.valueobjects;

public record MaintenanceQuoteId(Long value) {
    public MaintenanceQuoteId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MaintenanceQuoteId must be a positive number");
        }
    }
}