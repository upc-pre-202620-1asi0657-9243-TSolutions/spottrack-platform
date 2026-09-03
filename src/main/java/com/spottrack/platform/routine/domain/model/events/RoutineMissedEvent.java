package com.spottrack.platform.routine.domain.model.events;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;

public record RoutineMissedEvent(
        Long routineSessionId,
        Long routineId
) {
    public static RoutineMissedEvent from(RoutineSession session) {
        return new RoutineMissedEvent(
                session.getId(),
                session.getRoutineId()
        );
    }
}
