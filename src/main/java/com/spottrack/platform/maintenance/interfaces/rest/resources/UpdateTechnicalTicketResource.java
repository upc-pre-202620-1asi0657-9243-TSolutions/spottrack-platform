package com.spottrack.platform.maintenance.interfaces.rest.resources;

import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Partial update of a technical ticket; null fields are left unchanged")
public record UpdateTechnicalTicketResource(
        @Schema(description = "New description", example = "Treadmill belt slips and makes noise")
        String description,
        @Schema(description = "New priority", example = "URGENT")
        TicketPriority priority,
        @Schema(description = "New type", example = "CORRECTIVE")
        TicketType type,
        @Schema(description = "Optional; sets or changes the technician without changing the status", example = "9b2e4c71-3f5a-4d8b-b6c0-1e7a2d9f4c35")
        String technicianId
) {
}
