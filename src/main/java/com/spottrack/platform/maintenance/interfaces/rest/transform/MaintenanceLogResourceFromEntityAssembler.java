package com.spottrack.platform.maintenance.interfaces.rest.transform;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceLog;
import com.spottrack.platform.maintenance.interfaces.rest.resources.MaintenanceLogResource;

public class MaintenanceLogResourceFromEntityAssembler {

    public static MaintenanceLogResource toResourceFromEntity(MaintenanceLog entity) {
        return new MaintenanceLogResource(
                entity.getLogId().uuid(),
                entity.getTicketId(),
                entity.getMaintenanceId(),
                entity.getNotes(),
                entity.getCost(),
                entity.getCompletedAt()
        );
    }
}
