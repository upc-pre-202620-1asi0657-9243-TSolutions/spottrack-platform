package com.spottrack.platform.maintenance.interfaces.rest.resources;

import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Technical ticket of a maintenance request")
public record TechnicalTicketResource(
        @Schema(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90")
        String id,
        @Schema(description = "Maintenance request identifier (UUID)", example = "3a9d6e10-2b4c-4f8e-a1d7-6c5b9e0f1a23")
        String maintenanceId,
        @Schema(description = "Equipment identifier (UUID)", example = "7e3a1c5d-9f2b-4e6a-b8d4-0c1f5a3e7b92")
        String equipmentId,
        @Schema(description = "Assigned technician identifier (UUID), null when unassigned", example = "9b2e4c71-3f5a-4d8b-b6c0-1e7a2d9f4c35")
        String technicianId,
        @Schema(description = "Description of the problem", example = "Treadmill belt slips at high speed")
        String description,
        @Schema(description = "Ticket priority", example = "HIGH")
        TicketPriority priority,
        @Schema(description = "Ticket type", example = "CORRECTIVE")
        TicketType type,
        @Schema(description = "Ticket status", example = "IN_PROGRESS")
        TicketStatus ticketStatus,
        @Schema(description = "Maintenance status", example = "IN_PROGRESS")
        MaintenanceStatus maintenanceStatus,
        @Schema(description = "When the ticket was created", example = "2026-11-27T09:15:00")
        LocalDateTime createdAt,
        @Schema(description = "When work started, null if not started", example = "2026-11-28T08:00:00")
        LocalDateTime startedAt
) {}
