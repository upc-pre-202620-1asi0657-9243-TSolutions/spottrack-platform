package com.spottrack.platform.analytics.domain.model.commands;

public record RequestDowntimeCostCommand(Integer minutesInactive, String reason) {
    public RequestDowntimeCostCommand {
        if (minutesInactive == null || minutesInactive < 0) {
            throw new IllegalArgumentException("Minutes inactive cannot be negative");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be empty");
        }
    }
}