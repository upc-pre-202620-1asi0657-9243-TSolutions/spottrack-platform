package com.spottrack.platform.monitoring.domain.model.commands;

import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;

public record MarkSensorPairingUsedCommand(MotionSensorId motionSensorId) {
}
