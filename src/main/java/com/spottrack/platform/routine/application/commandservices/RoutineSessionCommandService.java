package com.spottrack.platform.routine.application.commandservices;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.domain.model.commands.CompleteRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.MarkRoutineMissedCommand;
import com.spottrack.platform.routine.domain.model.commands.SetExerciseBlockCompletionCommand;
import com.spottrack.platform.routine.domain.model.commands.StartRoutineCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface RoutineSessionCommandService {
    Result<RoutineSession, ApplicationError> handle(StartRoutineCommand command);
    Result<RoutineSession, ApplicationError> handle(CompleteRoutineCommand command);
    Result<RoutineSession, ApplicationError> handle(MarkRoutineMissedCommand command);
    Result<RoutineSession, ApplicationError> handle(SetExerciseBlockCompletionCommand command);
}
