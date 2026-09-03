package com.spottrack.platform.monitoring.domain.model.commands;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;

public record CameraCaptureMotionCommand(
        EquipmentId equipmentId,
        boolean movementDetectedViaVideo
) {
}
