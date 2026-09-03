package com.spottrack.platform.maintenance.domain.repositories;

import com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepository {
    Optional<Maintenance> findByMaintenanceId(MaintenanceId maintenanceId);
    Optional<Maintenance> findById(Long id);
    List<Maintenance> findAll();
    Maintenance save(Maintenance maintenance);
}
