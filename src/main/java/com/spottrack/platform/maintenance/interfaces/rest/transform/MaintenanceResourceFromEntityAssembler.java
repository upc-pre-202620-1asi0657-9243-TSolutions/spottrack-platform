package com.spottrack.platform.maintenance.interfaces.rest.transform;

import com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance;
import com.spottrack.platform.maintenance.interfaces.rest.resources.MaintenanceResource;

public class MaintenanceResourceFromEntityAssembler {

    public static MaintenanceResource toResourceFromEntity(Maintenance maintenance) {
        return new MaintenanceResource(
                maintenance.getId().uuid(),
                maintenance.getEquipmentId().uuid(),
                maintenance.getRequestedBy(),
                maintenance.getDescription()
        );
    }
}
