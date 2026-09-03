package com.spottrack.platform.analytics.domain.model.commands;

public record DetectHighDemandSlotCommand(String timeSlot, Integer estimatedUsers) {
    public DetectHighDemandSlotCommand {
        if (timeSlot == null || timeSlot.isBlank()) {
            throw new IllegalArgumentException("Time slot cannot be null or empty");
        }
        if (estimatedUsers == null || estimatedUsers < 0) {
            throw new IllegalArgumentException("Estimated users cannot be negative");
        }
    }
}