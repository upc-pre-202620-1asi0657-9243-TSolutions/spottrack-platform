package com.spottrack.platform.analytics.domain.model.commands;

public record DetectLowDemandSlotCommand(String timeSlot, Integer estimatedUsers) {
    public DetectLowDemandSlotCommand {
        if (timeSlot == null || timeSlot.isBlank()) {
            throw new IllegalArgumentException("Time slot cannot be null or empty");
        }
        if (estimatedUsers == null || estimatedUsers < 0) {
            throw new IllegalArgumentException("Estimated users cannot be negative");
        }
    }
}