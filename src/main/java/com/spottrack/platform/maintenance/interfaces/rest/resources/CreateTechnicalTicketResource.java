package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource for creating the technical ticket of a maintenance request")
public record CreateTechnicalTicketResource(
        @Schema(description = "Maintenance request identifier (UUID)", example = "3a9d6e10-2b4c-4f8e-a1d7-6c5b9e0f1a23")
        String maintenanceId,
        @Schema(description = "Ticket priority: LOW, MEDIUM, HIGH or URGENT", example = "HIGH")
        String priority,
        @Schema(description = "Ticket type: CORRECTIVE or PREVENTIVE", example = "CORRECTIVE")
        String type
) {}
