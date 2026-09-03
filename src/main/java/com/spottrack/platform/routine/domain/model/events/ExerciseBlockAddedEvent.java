package com.spottrack.platform.routine.domain.model.events;

import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.domain.model.entities.ExerciseBlock;

public record ExerciseBlockAddedEvent(
        Long routineId,
        Long exerciseBlockId,
        String exerciseName,
        String exerciseType
) {
    public static ExerciseBlockAddedEvent from(Routine routine, ExerciseBlock block) {
        return new ExerciseBlockAddedEvent(
                routine.getId(),
                block.getId(),
                block.getExerciseName().exerciseName(),
                block.getExerciseType().name()
        );
    }
}