package com.spottrack.platform.analytics.domain.model.commands;

public record RequestPercentageComparisonCommand(Double percentageChange) {
    public RequestPercentageComparisonCommand {
        if (percentageChange == null) {
            throw new IllegalArgumentException("Percentage change cannot be null");
        }
    }
}