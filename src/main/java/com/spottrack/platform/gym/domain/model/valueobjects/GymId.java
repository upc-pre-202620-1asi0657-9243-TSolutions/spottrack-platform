package com.spottrack.platform.gym.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record GymId(String uuid) {
    private static final String NOT_BLANK = "gym.error.gymId.notBlank";

    public GymId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK);
        }
    }
}
