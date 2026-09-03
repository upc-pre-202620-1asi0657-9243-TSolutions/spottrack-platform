package com.spottrack.platform.routine.interfaces.rest.transform;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.interfaces.rest.resources.RoutineSessionResource;

public class RoutineSessionResourceFromEntityAssembler {

    public static RoutineSessionResource toResourceFromEntity(RoutineSession session) {
        return new RoutineSessionResource(
                session.getId(),
                session.getRoutineId(),
                session.getClientId().clientId(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getCompletedExerciseBlockIds()
        );
    }
}
