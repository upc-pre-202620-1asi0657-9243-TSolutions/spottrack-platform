package com.spottrack.platform.maintenance.domain.model.commands;

import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;

/**
 * Edits a ticket's working data. technicianId is optional: when present it
 * sets or changes the technician without moving the ticket between statuses.
 */
public record UpdateTechnicalTicket(
        TechnicalTicketId ticketId,
        String description,
        TicketPriority priority,
        TicketType type,
        String technicianId
) {
    public UpdateTechnicalTicket {
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("maintenance.command.updateTechnicalTicket.description.notBlank");
        if (priority == null || type == null)
            throw new IllegalArgumentException("maintenance.command.updateTechnicalTicket.required");
    }
}
