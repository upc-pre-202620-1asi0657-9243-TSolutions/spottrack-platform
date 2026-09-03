package com.spottrack.platform.analytics.domain.model.commands;

public record RecommendTransferCommand(String operationalAdvice, String targetZone) {
    public RecommendTransferCommand {
        if (operationalAdvice == null || operationalAdvice.isBlank()) {
            throw new IllegalArgumentException("Operational advice cannot be empty");
        }
        if (targetZone == null || targetZone.isBlank()) {
            throw new IllegalArgumentException("Target zone cannot be empty");
        }
    }
}