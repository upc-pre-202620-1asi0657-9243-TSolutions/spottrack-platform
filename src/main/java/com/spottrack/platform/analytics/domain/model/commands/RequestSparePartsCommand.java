package com.spottrack.platform.analytics.domain.model.commands;

public record RequestSparePartsCommand(String partName, Integer quantity, Double unitPrice) {
    public RequestSparePartsCommand {
        if (partName == null || partName.isBlank()) {
            throw new IllegalArgumentException("Part name cannot be empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
}