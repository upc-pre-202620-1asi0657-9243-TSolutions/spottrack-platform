package com.spottrack.platform.gym.domain.model.valueobjects;

public record Dni(String value) {
    private static final String PATTERN = "^[0-9]{8}$";

    public Dni {
        if (value == null || !value.matches(PATTERN)) {
            throw new IllegalArgumentException("gym.error.dni.invalidFormat");
        }
    }
}
