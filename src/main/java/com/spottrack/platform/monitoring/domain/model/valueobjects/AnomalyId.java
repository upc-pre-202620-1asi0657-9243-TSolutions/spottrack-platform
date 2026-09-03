package com.spottrack.platform.monitoring.domain.model.valueobjects;

import java.util.UUID;

public record AnomalyId(String uuid) {
    public AnomalyId() {
        this(UUID.randomUUID().toString());
    }

    public AnomalyId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("anomaly.error.anomalyId.notBlank");
        }
    }
}
