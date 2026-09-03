package com.spottrack.platform.analytics.domain.model.commands;

/**
 * Keyed by equipmentId (not a report id) so both the manual REST endpoint
 * and the automatic monitoring-event listener can find-or-create the right
 * ActivityReport and accumulate minutes onto it the same way.
 */
public record RequestTotalUsageTimeCommand(String equipmentId, Integer minutesActive) {
    public RequestTotalUsageTimeCommand {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id cannot be blank");
        }
        if (minutesActive == null || minutesActive < 0) {
            throw new IllegalArgumentException("Minutes active cannot be negative");
        }
    }
}