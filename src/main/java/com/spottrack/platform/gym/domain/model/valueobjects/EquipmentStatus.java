package com.spottrack.platform.gym.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

public enum EquipmentStatus {
    AVAILABLE,
    OUT_OF_SERVICE,
    MAINTENANCE,
    ACTIVE,
    OCCUPIED,
    DECOMMISSIONED
}
