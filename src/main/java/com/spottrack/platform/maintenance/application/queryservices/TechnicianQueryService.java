package com.spottrack.platform.maintenance.application.queryservices;

import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllTechniciansQuery;

import java.util.List;

public interface TechnicianQueryService {
    List<Technician> handle(GetAllTechniciansQuery query);
}
