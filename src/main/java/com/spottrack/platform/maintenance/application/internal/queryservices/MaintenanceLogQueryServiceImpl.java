package com.spottrack.platform.maintenance.application.internal.queryservices;

import com.spottrack.platform.maintenance.application.queryservices.MaintenanceLogQueryService;
import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceLog;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllMaintenanceLogsQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetMaintenanceLogsByTicketIdQuery;
import com.spottrack.platform.maintenance.domain.repositories.MaintenanceLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceLogQueryServiceImpl implements MaintenanceLogQueryService {

    private final MaintenanceLogRepository maintenanceLogRepository;

    public MaintenanceLogQueryServiceImpl(MaintenanceLogRepository maintenanceLogRepository) {
        this.maintenanceLogRepository = maintenanceLogRepository;
    }

    @Override
    public List<MaintenanceLog> handle(GetMaintenanceLogsByTicketIdQuery query) {
        return maintenanceLogRepository.findByTicketId(query.ticketId());
    }

    @Override
    public List<MaintenanceLog> handle(GetAllMaintenanceLogsQuery query) {
        return maintenanceLogRepository.findAll();
    }
}
