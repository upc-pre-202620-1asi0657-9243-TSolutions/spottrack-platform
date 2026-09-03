package com.spottrack.platform.monitoring.domain.model.commands;

public record RegisterWeightReadingCommand(
        boolean weightDetected
) {
}
