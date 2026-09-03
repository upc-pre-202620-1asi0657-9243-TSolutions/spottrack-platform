package com.spottrack.platform.routine.domain.repositories;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;

import java.util.List;
import java.util.Optional;

public interface RoutineSessionRepository {
    Optional<RoutineSession> findById(Long id);
    List<RoutineSession> findAllByClientId(ClientId clientId);
    RoutineSession save(RoutineSession session);
}
