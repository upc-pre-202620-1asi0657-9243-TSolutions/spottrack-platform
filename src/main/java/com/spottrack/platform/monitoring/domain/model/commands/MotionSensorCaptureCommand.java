package com.spottrack.platform.monitoring.domain.model.commands;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;

public record MotionSensorCaptureCommand(
        EquipmentId equipmentId,
        boolean movementDetectedViaSensor
) {
}
