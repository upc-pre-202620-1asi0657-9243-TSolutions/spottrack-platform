package com.spottrack.platform.monitoring.application.internal.queryservices;

import com.spottrack.platform.monitoring.application.queryServices.MotionSensorQueryService;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllMotionSensorsQuery;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotionSensorQueryServiceImpl implements MotionSensorQueryService {
    private final MotionSensorRepository motionSensorRepository;

    public MotionSensorQueryServiceImpl(MotionSensorRepository motionSensorRepository) {
        this.motionSensorRepository = motionSensorRepository;
    }

    @Override
    public List<MotionSensor> handle(GetAllMotionSensorsQuery query) {
        return motionSensorRepository.findAll();
    }
}
