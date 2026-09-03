package com.spottrack.platform.routine.interfaces.rest.transform;

import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.interfaces.rest.resources.RoutineResource;

public class RoutineResourceFromEntityAssembler {

    public static RoutineResource toResourceFromEntity(Routine entity) {
        return new RoutineResource(
                entity.getId(),
                entity.getRoutineName().routineName(),
                entity.getClientId().clientId(),
                entity.getExerciseBlocks().size());
    }
}
