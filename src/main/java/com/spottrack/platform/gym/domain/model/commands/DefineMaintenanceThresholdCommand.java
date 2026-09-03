package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;

import java.time.LocalDate;

public record DefineMaintenanceThresholdCommand(EquipmentId equipmentId, LocalDate threshold) {
    public DefineMaintenanceThresholdCommand {

        if (threshold == null) {
            throw new IllegalArgumentException("equipment.command.defineMaintenanceThreshold.threshold.notNull");
        }
    }
}
