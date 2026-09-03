package com.spottrack.platform.routine.domain.model.events;

import com.spottrack.platform.routine.domain.model.aggregates.Routine;

public record RoutineCreatedEvent(
        Long routineId,
        String routineName,
        Long clientId
) {
    public static RoutineCreatedEvent from(Routine routine) {
        return new RoutineCreatedEvent(
                routine.getId(),
                routine.getRoutineName().routineName(),
                routine.getClientId().clientId()
        );
    }
}