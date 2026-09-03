package com.spottrack.platform.routine.domain.model.events;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;

public record RoutineCompletedEvent(
        Long routineSessionId,
        Long routineId
) {
    public static RoutineCompletedEvent from(RoutineSession session) {
        return new RoutineCompletedEvent(
                session.getId(),
                session.getRoutineId()
        );
    }
}
