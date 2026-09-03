package com.spottrack.platform.maintenance.domain.repositories;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceLog;

import java.util.List;

public interface MaintenanceLogRepository {
    MaintenanceLog save(MaintenanceLog log);
    List<MaintenanceLog> findByTicketId(String ticketId);
    List<MaintenanceLog> findAll();
}
