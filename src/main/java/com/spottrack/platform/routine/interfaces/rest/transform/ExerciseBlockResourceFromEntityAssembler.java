package com.spottrack.platform.routine.interfaces.rest.transform;

import com.spottrack.platform.routine.domain.model.entities.ExerciseBlock;
import com.spottrack.platform.routine.interfaces.rest.resources.ExerciseBlockResource;

public class ExerciseBlockResourceFromEntityAssembler {

    public static ExerciseBlockResource toResourceFromEntity(ExerciseBlock entity) {
        return new ExerciseBlockResource(
                entity.getId(),
                entity.getExerciseName().exerciseName(),
                entity.getExerciseType().name(),
                entity.getOrder(),
                entity.getSets(),
                entity.getReps());
    }
}
