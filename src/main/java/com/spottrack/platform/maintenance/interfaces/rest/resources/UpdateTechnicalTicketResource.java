package com.spottrack.platform.maintenance.interfaces.rest.resources;

import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateTechnicalTicketResource(
        @Schema
        String description,
        @Schema
        TicketPriority priority,
        @Schema
        TicketType type,
        @Schema(description = "Optional; sets or changes the technician without changing the status")
        String technicianId
) {
}
