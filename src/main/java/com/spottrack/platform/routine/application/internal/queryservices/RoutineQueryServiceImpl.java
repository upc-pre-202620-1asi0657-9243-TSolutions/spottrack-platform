package com.spottrack.platform.routine.application.internal.queryservices;

import com.spottrack.platform.routine.application.queryservices.RoutineQueryService;
import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.domain.model.queries.GetAllRoutinesByClientIdQuery;
import com.spottrack.platform.routine.domain.model.queries.GetRoutineByIdQuery;
import com.spottrack.platform.routine.domain.repositories.RoutineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoutineQueryServiceImpl implements RoutineQueryService {
    private final RoutineRepository routineRepository;
    public RoutineQueryServiceImpl(RoutineRepository routineRepository) {
        this.routineRepository = routineRepository;
    }
    @Override
    public Optional<Routine> handle(GetRoutineByIdQuery query) {
        return routineRepository.findById(query.routineId());
    }
    @Override
    public List<Routine> handle(GetAllRoutinesByClientIdQuery query) {
        return routineRepository.findAllByClientId((query.clientId()));
    }
}
