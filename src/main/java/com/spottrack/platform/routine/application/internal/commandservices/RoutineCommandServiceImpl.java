package com.spottrack.platform.routine.application.internal.commandservices;

import com.spottrack.platform.routine.application.commandservices.RoutineCommandService;
import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.domain.model.commands.AddExerciseBlockCommand;
import com.spottrack.platform.routine.domain.model.commands.CreateRoutineCommand;
import com.spottrack.platform.routine.domain.model.entities.ExerciseBlock;
import com.spottrack.platform.routine.domain.repositories.RoutineRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class RoutineCommandServiceImpl implements RoutineCommandService {
    private final RoutineRepository routineRepository;
    public RoutineCommandServiceImpl(RoutineRepository routineRepository) {
        this.routineRepository = routineRepository;
    }
    @Override
    public Result<Routine, ApplicationError> handle(CreateRoutineCommand command) {
        try {
            // validaciones
            var routine = new Routine(command);
            var savedRoutine= routineRepository.save(routine);
            return Result.success(savedRoutine);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Routine", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Routine creation", e.getMessage()));
        }
    }

    @Override
    public Result<ExerciseBlock, ApplicationError> handle(AddExerciseBlockCommand command) {
        try {
            var routine = routineRepository.findById(command.routineId());

            if (routine.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Routine", command.routineId().toString()));
            }

            routine.get().addExerciseBlock(command.exerciseName(), command.exerciseType(), command.order(), command.sets(), command.reps());

            var savedRoutine = routineRepository.save(routine.get());

            return Result.success(savedRoutine.getExerciseBlocks().getLast());

        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("ExerciseBlock", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("ExerciseBlock creation", e.getMessage()));
        }
    }

}
