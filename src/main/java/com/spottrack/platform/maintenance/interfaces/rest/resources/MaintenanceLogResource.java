package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Completion log of the work done on a ticket")
public record MaintenanceLogResource(
        @Schema(description = "Maintenance log identifier (UUID)", example = "e1f7a3b9-4c2d-4e8a-9b6f-5d0c3a7e1b24")
        String id,
        @Schema(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90")
        String ticketId,
        @Schema(description = "Maintenance request identifier (UUID)", example = "3a9d6e10-2b4c-4f8e-a1d7-6c5b9e0f1a23")
        String maintenanceId,
        @Schema(description = "Notes about the work done", example = "Replaced the drive belt and recalibrated the speed sensor")
        String notes,
        @Schema(description = "Cost of the work", example = "180.50")
        BigDecimal cost,
        @Schema(description = "When the work was completed", example = "2026-11-28T15:30:00")
        LocalDateTime completedAt
) {
}
