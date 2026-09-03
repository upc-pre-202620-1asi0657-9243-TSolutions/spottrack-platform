package com.spottrack.platform.analytics.domain.model.commands;

public record RequestRoiCommand(Double expectedRoiPercentage) {
    public RequestRoiCommand {
        if (expectedRoiPercentage == null) {
            throw new IllegalArgumentException("Expected ROI percentage cannot be null");
        }
    }
}