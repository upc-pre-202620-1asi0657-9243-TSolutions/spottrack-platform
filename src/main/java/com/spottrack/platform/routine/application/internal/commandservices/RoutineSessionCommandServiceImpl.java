package com.spottrack.platform.routine.application.internal.commandservices;

import com.spottrack.platform.routine.application.commandservices.RoutineSessionCommandService;
import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.domain.model.commands.CompleteRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.MarkRoutineMissedCommand;
import com.spottrack.platform.routine.domain.model.commands.SetExerciseBlockCompletionCommand;
import com.spottrack.platform.routine.domain.model.commands.StartRoutineCommand;
import com.spottrack.platform.routine.domain.repositories.RoutineRepository;
import com.spottrack.platform.routine.domain.repositories.RoutineSessionRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class RoutineSessionCommandServiceImpl implements RoutineSessionCommandService {

    private final RoutineSessionRepository routineSessionRepository;
    private final RoutineRepository routineRepository;

    public RoutineSessionCommandServiceImpl(RoutineSessionRepository routineSessionRepository,
                                             RoutineRepository routineRepository) {
        this.routineSessionRepository = routineSessionRepository;
        this.routineRepository = routineRepository;
    }

    @Override
    public Result<RoutineSession, ApplicationError> handle(StartRoutineCommand command) {
        try {
            var routineOpt = routineRepository.findById(command.routineId());
            if (routineOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Routine", command.routineId().toString()));
            }
            if (!routineOpt.get().getClientId().equals(command.clientId())) {
                return Result.failure(ApplicationError.forbidden("Routine", "routineId:" + command.routineId()));
            }
            var session = new RoutineSession(command);
            var savedSession = routineSessionRepository.save(session);
            return Result.success(savedSession);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("RoutineSession", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("RoutineSession creation", e.getMessage()));
        }
    }

    @Override
    public Result<RoutineSession, ApplicationError> handle(CompleteRoutineCommand command) {
        try {
            var session = routineSessionRepository.findById(command.routineSessionId());
            if (session.isEmpty()) {
                return Result.failure(ApplicationError.notFound("RoutineSession", command.routineSessionId().toString()));
            }
            session.get().complete();
            var savedSession = routineSessionRepository.save(session.get());
            return Result.success(savedSession);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("RoutineSession", e.getMessage()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.conflict("RoutineSession", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("RoutineSession completion", e.getMessage()));
        }
    }

    @Override
    public Result<RoutineSession, ApplicationError> handle(MarkRoutineMissedCommand command) {
        try {
            var session = routineSessionRepository.findById(command.routineSessionId());
            if (session.isEmpty()) {
                return Result.failure(ApplicationError.notFound("RoutineSession", command.routineSessionId().toString()));
            }
            session.get().markMissed();
            var savedSession = routineSessionRepository.save(session.get());
            return Result.success(savedSession);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("RoutineSession", e.getMessage()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.conflict("RoutineSession", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("RoutineSession missed marking", e.getMessage()));
        }
    }

    @Override
    public Result<RoutineSession, ApplicationError> handle(SetExerciseBlockCompletionCommand command) {
        try {
            var sessionOpt = routineSessionRepository.findById(command.sessionId());
            if (sessionOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("RoutineSession", command.sessionId().toString()));
            }
            var session = sessionOpt.get();
            var routineOpt = routineRepository.findById(session.getRoutineId());
            if (routineOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Routine", session.getRoutineId().toString()));
            }
            boolean blockBelongsToRoutine = routineOpt.get().getExerciseBlocks().stream()
                    .anyMatch(block -> block.getId().equals(command.exerciseBlockId()));
            if (!blockBelongsToRoutine) {
                return Result.failure(ApplicationError.validationError(
                        "ExerciseBlock", "Exercise block does not belong to this routine"));
            }
            session.setExerciseCompletion(command.exerciseBlockId(), command.completed());
            var savedSession = routineSessionRepository.save(session);
            return Result.success(savedSession);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("RoutineSession", e.getMessage()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.conflict("RoutineSession", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Exercise completion update", e.getMessage()));
        }
    }
}
