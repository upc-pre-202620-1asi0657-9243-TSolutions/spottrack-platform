package com.spottrack.platform.monitoring.domain.model.valueobjects;

import java.util.UUID;

public record MotionSensorId(String uuid) {
    public MotionSensorId() {
        this(UUID.randomUUID().toString());
    }

    public MotionSensorId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("motionSensor.error.motionSensorId.notBlank");
        }
    }
}
