package com.spottrack.platform.analytics.domain.model.commands;

public record RequestActivityAnalysisCommand(
        String equipmentId,
        Integer minutesActive,
        Integer minutesInactive,
        String downtimeReason,
        Double percentageChange
) {
    public RequestActivityAnalysisCommand {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id cannot be blank");
        }
        if (minutesActive == null || minutesActive < 0) {
            throw new IllegalArgumentException("Minutes active cannot be negative");
        }
        if (minutesInactive == null || minutesInactive < 0) {
            throw new IllegalArgumentException("Minutes inactive cannot be negative");
        }
    }
}