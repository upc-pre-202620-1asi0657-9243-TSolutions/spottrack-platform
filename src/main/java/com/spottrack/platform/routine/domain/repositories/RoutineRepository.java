package com.spottrack.platform.routine.domain.repositories;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.model.aggregates.Routine;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository {
    Optional<Routine> findById(Long id);
    List<Routine> findAllByClientId(ClientId clientId);
    Routine save(Routine routine);
    boolean existsById(Long id);
}