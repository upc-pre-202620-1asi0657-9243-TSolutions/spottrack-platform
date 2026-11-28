package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resource for registering the completion log of a ticket")
public record RegisterMaintenanceCompletionResource(
        @Schema(description = "Maintenance request identifier (UUID)", example = "3a9d6e10-2b4c-4f8e-a1d7-6c5b9e0f1a23")
        String maintenanceId,
        @Schema(description = "Notes about the work done", example = "Replaced the drive belt and recalibrated the speed sensor")
        String notes,
        @Schema(description = "Cost of the work", example = "180.50")
        BigDecimal cost
) {}
