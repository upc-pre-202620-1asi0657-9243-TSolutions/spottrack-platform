package com.spottrack.platform.maintenance.interfaces.rest.transform;

import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnicalTicketCommand;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import com.spottrack.platform.maintenance.interfaces.rest.resources.CreateTechnicalTicketResource;

public class CreateTechnicalTicketCommandFromResourceAssembler {

    public static CreateTechnicalTicketCommand toCommandFromResource(CreateTechnicalTicketResource resource) {
        return new CreateTechnicalTicketCommand(
                resource.maintenanceId(),
                TicketPriority.valueOf(resource.priority()),
                TicketType.valueOf(resource.type())
        );
    }
}
