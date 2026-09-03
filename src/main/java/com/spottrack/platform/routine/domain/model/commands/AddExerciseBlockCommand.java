package com.spottrack.platform.routine.domain.model.commands;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseName;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;

public record AddExerciseBlockCommand(
        Long routineId,
        ExerciseName exerciseName,
        ExerciseType exerciseType,
        int order,
        int sets,
        int reps
) {}