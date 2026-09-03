package com.spottrack.platform.routine.domain.model.events;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;

public record RoutineStartedEvent(
        Long routineSessionId,
        Long routineId,
        Long clientId
) {
    public static RoutineStartedEvent from(RoutineSession session) {
        return new RoutineStartedEvent(
                session.getId(),
                session.getRoutineId(),
                session.getClientId().clientId()
        );
    }
}
