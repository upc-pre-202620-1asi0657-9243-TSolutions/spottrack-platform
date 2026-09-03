package com.spottrack.platform.monitoring.application.internal.queryservices;

import com.spottrack.platform.monitoring.application.queryServices.CameraSensorQueryService;
import com.spottrack.platform.monitoring.domain.model.aggregates.CameraSensor;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllCameraSensorsQuery;
import com.spottrack.platform.monitoring.domain.repositories.CameraSensorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CameraSensorQueryServiceImpl implements CameraSensorQueryService {
    private final CameraSensorRepository cameraSensorRepository;

    public CameraSensorQueryServiceImpl(CameraSensorRepository cameraSensorRepository) {
        this.cameraSensorRepository = cameraSensorRepository;
    }

    @Override
    public List<CameraSensor> handle(GetAllCameraSensorsQuery query) {
        return cameraSensorRepository.findAll();
    }
}
