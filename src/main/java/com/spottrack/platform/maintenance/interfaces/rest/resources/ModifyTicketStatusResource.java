package com.spottrack.platform.maintenance.interfaces.rest.resources;

import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource for changing a ticket status")
public record ModifyTicketStatusResource(
        @Schema(description = "New ticket status", example = "IN_PROGRESS")
        TicketStatus newStatus
) {}
