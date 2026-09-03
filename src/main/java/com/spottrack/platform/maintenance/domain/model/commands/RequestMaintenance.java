package com.spottrack.platform.maintenance.domain.model.commands;


import com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId;

public record RequestMaintenance(
        EquipmentId equipmentId,
        String requestedBy,
        String description
) {
    public RequestMaintenance {
        if (requestedBy == null || requestedBy.isBlank())
            throw new IllegalArgumentException("maintenance.command.requestMaintenance.requestedBy.notBlank");
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("maintenance.command.requestMaintenance.description.notBlank");
    }
}
