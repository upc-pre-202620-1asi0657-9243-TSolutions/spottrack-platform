package com.spottrack.platform.maintenance.domain.model.commands;

import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceJobId;

public record AcceptMaintenance(
        MaintenanceJobId maintenanceJobId,
        String technicianId
) {
    public AcceptMaintenance {
        if (technicianId == null || technicianId.isBlank())
            throw new IllegalArgumentException("maintenance.command.acceptMaintenance.technicianId.notBlank");
    }
}
