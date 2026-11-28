package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource for requesting maintenance for an equipment")
public record RequestMaintenanceResource(
        @Schema(description = "Equipment identifier (UUID)", example = "7e3a1c5d-9f2b-4e6a-b8d4-0c1f5a3e7b92")
        String equipmentId,
        @Schema(description = "Who requests the maintenance", example = "admin@spottrack.com")
        String requestedBy,
        @Schema(description = "Description of the problem", example = "Treadmill belt slips at high speed")
        String description
) {}
