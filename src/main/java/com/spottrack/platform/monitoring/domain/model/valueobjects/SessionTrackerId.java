package com.spottrack.platform.monitoring.domain.model.valueobjects;

public record SessionTrackerId(String uuid) {
    public SessionTrackerId {
        if (uuid == null || uuid.isBlank()){
            throw new IllegalArgumentException("uuid cant be null or blank");
        }
    }
}
