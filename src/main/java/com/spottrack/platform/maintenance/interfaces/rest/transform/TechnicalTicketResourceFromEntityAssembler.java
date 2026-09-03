package com.spottrack.platform.maintenance.interfaces.rest.transform;

import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.interfaces.rest.resources.TechnicalTicketResource;

public class TechnicalTicketResourceFromEntityAssembler {

    public static TechnicalTicketResource toResourceFromEntity(TechnicalTicket ticket) {
        return new TechnicalTicketResource(
                ticket.getTicketId().uuid(),
                ticket.getMaintenanceId(),
                ticket.getEquipmentId(),
                ticket.getTechnicianId(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getType(),
                ticket.getTicketStatus(),
                ticket.getMaintenanceStatus(),
                ticket.getCreatedAt()
        );
    }
}
