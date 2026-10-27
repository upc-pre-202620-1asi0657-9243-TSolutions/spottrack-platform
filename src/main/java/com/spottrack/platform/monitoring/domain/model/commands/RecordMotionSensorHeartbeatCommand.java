package com.spottrack.platform.monitoring.domain.model.commands;

import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;

import java.time.LocalDateTime;

public record RecordMotionSensorHeartbeatCommand(MotionSensorId motionSensorId, LocalDateTime seenAt) {
}
