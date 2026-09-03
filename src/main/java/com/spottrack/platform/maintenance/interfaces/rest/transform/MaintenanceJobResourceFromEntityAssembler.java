package com.spottrack.platform.maintenance.interfaces.rest.transform;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceJob;
import com.spottrack.platform.maintenance.interfaces.rest.resources.MaintenanceJobResource;

public class MaintenanceJobResourceFromEntityAssembler {

    public static MaintenanceJobResource toResourceFromEntity(MaintenanceJob entity) {
        return new MaintenanceJobResource(
                entity.getJobId().uuid(),
                entity.getMaintenanceId(),
                entity.getTechnicianId(),
                entity.isAccepted()
        );
    }
}
