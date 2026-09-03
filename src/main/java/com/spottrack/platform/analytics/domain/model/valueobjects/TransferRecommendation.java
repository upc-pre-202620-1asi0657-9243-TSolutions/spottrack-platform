package com.spottrack.platform.analytics.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record TransferRecommendation(String operationalAdvice, String targetZone) {
    public TransferRecommendation {
        if (operationalAdvice == null || operationalAdvice.isBlank()) {
            throw new IllegalArgumentException("Operational advice cannot be empty");
        }
        if (targetZone == null || targetZone.isBlank()) {
            throw new IllegalArgumentException("Target zone cannot be empty");
        }
    }
}