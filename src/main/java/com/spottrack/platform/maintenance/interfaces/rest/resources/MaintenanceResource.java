package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Maintenance request for an equipment")
public record MaintenanceResource(
        @Schema(description = "Maintenance request identifier (UUID)", example = "3a9d6e10-2b4c-4f8e-a1d7-6c5b9e0f1a23")
        String id,
        @Schema(description = "Equipment identifier (UUID)", example = "7e3a1c5d-9f2b-4e6a-b8d4-0c1f5a3e7b92")
        String equipmentId,
        @Schema(description = "Who requested the maintenance", example = "admin@spottrack.com")
        String requestedBy,
        @Schema(description = "Description of the problem", example = "Treadmill belt slips at high speed")
        String description
) {}
