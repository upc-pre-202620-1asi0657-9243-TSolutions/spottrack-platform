package com.spottrack.platform.maintenance.domain.model.commands;

import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;

public record CreateTechnicalTicketCommand(
        String maintenanceId,
        TicketPriority priority,
        TicketType type
) {
    public CreateTechnicalTicketCommand {
        if (maintenanceId == null || maintenanceId.isBlank())
            throw new IllegalArgumentException("maintenance.command.createTechnicalTicket.maintenanceId.notBlank");
        if (priority == null)
            throw new IllegalArgumentException("maintenance.command.createTechnicalTicket.priority.notNull");
        if (type == null)
            throw new IllegalArgumentException("maintenance.command.createTechnicalTicket.type.notNull");
    }
}
