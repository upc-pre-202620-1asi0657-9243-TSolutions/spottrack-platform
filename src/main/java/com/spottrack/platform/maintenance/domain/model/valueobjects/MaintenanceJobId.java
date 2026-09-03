package com.spottrack.platform.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record MaintenanceJobId(String uuid) {

    public MaintenanceJobId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("maintenance.error.maintenanceJobId.notBlank");
        }
    }
}
