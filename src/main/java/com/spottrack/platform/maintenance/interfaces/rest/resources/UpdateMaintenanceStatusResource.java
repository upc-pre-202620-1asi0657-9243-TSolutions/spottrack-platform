package com.spottrack.platform.maintenance.interfaces.rest.resources;

import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource for changing the maintenance status of a ticket")
public record UpdateMaintenanceStatusResource(
        @Schema(description = "New maintenance status", example = "COMPLETED")
        MaintenanceStatus newStatus
) {}
