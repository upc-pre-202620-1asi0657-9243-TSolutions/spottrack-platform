package com.spottrack.platform.routine.application.internal.queryservices;

import com.spottrack.platform.routine.application.queryservices.RoutineSessionQueryService;
import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.domain.model.queries.GetAllRoutineSessionsByClientIdQuery;
import com.spottrack.platform.routine.domain.model.queries.GetRoutineSessionByIdQuery;
import com.spottrack.platform.routine.domain.repositories.RoutineSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoutineSessionQueryServiceImpl implements RoutineSessionQueryService {

    private final RoutineSessionRepository routineSessionRepository;

    public RoutineSessionQueryServiceImpl(RoutineSessionRepository routineSessionRepository) {
        this.routineSessionRepository = routineSessionRepository;
    }

    @Override
    public Optional<RoutineSession> handle(GetRoutineSessionByIdQuery query) {
        return routineSessionRepository.findById(query.routineSessionId());
    }

    @Override
    public List<RoutineSession> handle(GetAllRoutineSessionsByClientIdQuery query) {
        return routineSessionRepository.findAllByClientId(query.clientId());
    }
}
