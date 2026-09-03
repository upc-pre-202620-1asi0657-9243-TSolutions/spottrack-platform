package com.spottrack.platform.monitoring.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ZoneId(String uuid) {
    private static final String NOT_BLANK = "zone.error.zoneId.notBlank";

    public ZoneId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK);
        }
    }
}
