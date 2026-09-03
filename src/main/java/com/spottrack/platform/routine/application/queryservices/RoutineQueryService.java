package com.spottrack.platform.routine.application.queryservices;

import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.domain.model.queries.GetAllRoutinesByClientIdQuery;
import com.spottrack.platform.routine.domain.model.queries.GetRoutineByIdQuery;

import java.util.List;
import java.util.Optional;

public interface RoutineQueryService {
    Optional<Routine> handle(GetRoutineByIdQuery query);
    List<Routine> handle(GetAllRoutinesByClientIdQuery query);
}