package com.spottrack.platform.analytics.domain.model.commands;

public record RequestEarningsCommand(Double projectedRevenue) {
    public RequestEarningsCommand {
        if (projectedRevenue == null || projectedRevenue < 0) {
            throw new IllegalArgumentException("Projected revenue cannot be negative");
        }
    }
}