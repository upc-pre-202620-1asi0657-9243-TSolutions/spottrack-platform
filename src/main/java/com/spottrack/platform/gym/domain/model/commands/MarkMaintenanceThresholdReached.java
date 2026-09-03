package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;

public record MarkMaintenanceThresholdReached(EquipmentId equipmentId) {
}
