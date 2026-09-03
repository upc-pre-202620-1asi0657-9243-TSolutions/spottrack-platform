package com.spottrack.platform.analytics.domain.model.commands;

public record RequestReportRoiCommand(
        Double expectedRoiPercentage,
        Double projectedRevenue,
        String timeSlot,
        Integer estimatedUsers,
        String operationalAdvice,
        String targetZone
) {
    public RequestReportRoiCommand {
        if (expectedRoiPercentage == null) {
            throw new IllegalArgumentException("Expected ROI percentage cannot be null");
        }
        if (projectedRevenue == null || projectedRevenue < 0) {
            throw new IllegalArgumentException("Projected revenue cannot be negative");
        }
    }
}