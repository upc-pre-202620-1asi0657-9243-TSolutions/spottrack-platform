package com.spottrack.platform.analytics.domain.model.commands;

public record RequestMaintenanceCostCommand(Double amount, String currency) {
    public RequestMaintenanceCostCommand {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Maintenance cost amount cannot be negative");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }
    }
}