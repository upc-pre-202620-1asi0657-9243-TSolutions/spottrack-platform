package com.spottrack.platform.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record MaintenanceId(String uuid) {

    public MaintenanceId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("maintenance.error.maintenanceId.notBlank");
        }
    }
}
