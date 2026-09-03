package com.spottrack.platform.analytics.domain.model.commands;

public record RequestADetailedMaintenanceQuoteCommand(
        String equipmentId,
        Double amount,
        String currency,
        String costTypeValue,
        String partName,
        Integer quantity,
        Double unitPrice
) {
    public RequestADetailedMaintenanceQuoteCommand {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id cannot be blank");
        }
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
}