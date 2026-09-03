package com.spottrack.platform.maintenance.domain.model.commands;

import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;

public record AssignTechnicalTicket(
        TechnicalTicketId ticketId,
        String technicianId
) {
    public AssignTechnicalTicket {
        if (technicianId == null || technicianId.isBlank())
            throw new IllegalArgumentException("maintenance.command.assignTechnicalTicket.technicianId.notBlank");
    }
}
