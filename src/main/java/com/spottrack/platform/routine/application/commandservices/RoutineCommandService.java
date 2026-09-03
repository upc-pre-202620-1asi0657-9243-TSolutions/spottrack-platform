package com.spottrack.platform.routine.application.commandservices;

import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.domain.model.commands.AddExerciseBlockCommand;
import com.spottrack.platform.routine.domain.model.commands.CreateRoutineCommand;
import com.spottrack.platform.routine.domain.model.entities.ExerciseBlock;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface RoutineCommandService {
    Result<Routine, ApplicationError> handle(CreateRoutineCommand command);
    Result<ExerciseBlock, ApplicationError> handle(AddExerciseBlockCommand command);
}