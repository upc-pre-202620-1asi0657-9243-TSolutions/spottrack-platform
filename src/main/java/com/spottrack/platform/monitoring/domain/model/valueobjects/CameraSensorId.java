package com.spottrack.platform.monitoring.domain.model.valueobjects;

import java.util.UUID;

public record CameraSensorId(String uuid) {
    public CameraSensorId() {
        this(UUID.randomUUID().toString());
    }

    public CameraSensorId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("cameraSensor.error.cameraSensorId.notBlank");
        }
    }
}
