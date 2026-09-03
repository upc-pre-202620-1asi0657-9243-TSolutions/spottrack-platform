package com.spottrack.platform.routine.domain.model.valueobjects;

public record ExerciseName(String exerciseName) {
    public ExerciseName {
        if (exerciseName == null || exerciseName.isBlank()) {
            throw new IllegalArgumentException("Exercise name must not be null or blank");
        }
        if (exerciseName.length() > 100) {
            throw new IllegalArgumentException("Exercise name must not exceed 100 characters");
        }
    }
}