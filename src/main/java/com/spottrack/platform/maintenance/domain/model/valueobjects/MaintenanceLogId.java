package com.spottrack.platform.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record MaintenanceLogId(String uuid) {

    public MaintenanceLogId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("maintenance.error.maintenanceLogId.notBlank");
        }
    }
}
