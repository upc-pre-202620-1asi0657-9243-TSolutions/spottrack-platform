package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.BranchId;

public record AddZoneCommand(String zoneName, int maximumOccupancy, BranchId branchId) {
    public AddZoneCommand {
        if (zoneName == null || zoneName.isBlank()) {
            throw new IllegalArgumentException("zoneName must not be null or blank");
        }
        if (maximumOccupancy <= 0) {
            throw new IllegalArgumentException("maximumOccupancy must be greater than zero");
        }
    }
}
