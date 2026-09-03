package com.spottrack.platform.monitoring.application.queryServices;

import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllMotionSensorsQuery;

import java.util.List;

public interface MotionSensorQueryService {
    List<MotionSensor> handle(GetAllMotionSensorsQuery query);
}
