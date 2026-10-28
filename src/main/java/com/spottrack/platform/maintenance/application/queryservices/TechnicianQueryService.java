package com.spottrack.platform.maintenance.application.queryservices;

import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllTechniciansQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechnicianByTechnicianIdQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechnicianByUserIdQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechniciansByAdminQuery;

import java.util.List;
import java.util.Optional;

public interface TechnicianQueryService {
    List<Technician> handle(GetAllTechniciansQuery query);
    List<Technician> handle(GetTechniciansByAdminQuery query);
    Optional<Technician> handle(GetTechnicianByUserIdQuery query);
    Optional<Technician> handle(GetTechnicianByTechnicianIdQuery query);
}
