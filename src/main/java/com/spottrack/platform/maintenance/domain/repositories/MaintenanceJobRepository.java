package com.spottrack.platform.maintenance.domain.repositories;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceJob;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceJobId;

import java.util.Optional;

public interface MaintenanceJobRepository {
    Optional<MaintenanceJob> findById(MaintenanceJobId id);
    MaintenanceJob save(MaintenanceJob job);
}
