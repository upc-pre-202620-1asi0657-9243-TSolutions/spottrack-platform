package com.spottrack.platform.routine.interfaces.rest.transform;

import com.spottrack.platform.routine.domain.model.commands.AddExerciseBlockCommand;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseName;
import com.spottrack.platform.routine.interfaces.rest.resources.AddExerciseBlockResource;

public class AddExerciseBlockCommandFromResourceAssembler {

    public static AddExerciseBlockCommand toCommandFromResource(AddExerciseBlockResource resource) {
        return new AddExerciseBlockCommand(
                resource.routineId(),
                new ExerciseName(resource.exerciseName()),
                resource.exerciseType(),
                resource.order(),
                resource.sets(),
                resource.reps());
    }
}
