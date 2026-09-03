package com.spottrack.platform.maintenance.application.queryservices;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceLog;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllMaintenanceLogsQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetMaintenanceLogsByTicketIdQuery;

import java.util.List;

public interface MaintenanceLogQueryService {
    List<MaintenanceLog> handle(GetMaintenanceLogsByTicketIdQuery query);
    List<MaintenanceLog> handle(GetAllMaintenanceLogsQuery query);
}
