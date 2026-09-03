package com.spottrack.platform.monitoring.application.queryServices;

import com.spottrack.platform.monitoring.domain.model.aggregates.CameraSensor;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllCameraSensorsQuery;

import java.util.List;

public interface CameraSensorQueryService {
    List<CameraSensor> handle(GetAllCameraSensorsQuery query);
}
