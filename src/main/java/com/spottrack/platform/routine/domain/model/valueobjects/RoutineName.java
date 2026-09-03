package com.spottrack.platform.routine.domain.model.valueobjects;

public record RoutineName(String routineName) {
    public RoutineName {
        if (routineName == null || routineName.isBlank()) {
            throw new IllegalArgumentException("Routine name must not be null or blank");
        }
        if (routineName.length() > 100) {
            throw new IllegalArgumentException("Routine name must not exceed 100 characters");
        }
    }
}