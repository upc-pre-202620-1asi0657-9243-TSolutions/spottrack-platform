package com.spottrack.platform.routine.application.queryservices;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.domain.model.queries.GetAllRoutineSessionsByClientIdQuery;
import com.spottrack.platform.routine.domain.model.queries.GetRoutineSessionByIdQuery;

import java.util.List;
import java.util.Optional;

public interface RoutineSessionQueryService {
    Optional<RoutineSession> handle(GetRoutineSessionByIdQuery query);
    List<RoutineSession> handle(GetAllRoutineSessionsByClientIdQuery query);
}
