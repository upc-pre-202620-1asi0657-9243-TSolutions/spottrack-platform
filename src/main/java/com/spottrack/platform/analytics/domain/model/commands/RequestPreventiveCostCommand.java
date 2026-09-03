package com.spottrack.platform.analytics.domain.model.commands;

public record RequestPreventiveCostCommand(Double amount, String currency) {
    public RequestPreventiveCostCommand {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Preventive cost amount cannot be negative");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }
    }
}