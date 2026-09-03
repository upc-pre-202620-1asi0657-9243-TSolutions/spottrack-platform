package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record SparePartDetail(String partName, Integer quantity, Double unitPrice) {
    public SparePartDetail {
        if (partName == null || partName.isBlank()) {
            throw new IllegalArgumentException("Spare part name cannot be empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
}