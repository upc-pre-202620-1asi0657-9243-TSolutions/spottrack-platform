package com.spottrack.platform.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record TechnicianId(String uuid) {

    public TechnicianId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("maintenance.error.technicianId.notBlank");
        }
    }
}
