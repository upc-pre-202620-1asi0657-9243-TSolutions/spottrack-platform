package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Maintenance job accepted by a technician")
public record MaintenanceJobResource(
        @Schema(description = "Maintenance job identifier (UUID)", example = "c4d8e2a1-6b3f-4a9c-8e57-2f1b0d6a9e48")
        String id,
        @Schema(description = "Maintenance request identifier (UUID)", example = "3a9d6e10-2b4c-4f8e-a1d7-6c5b9e0f1a23")
        String maintenanceId,
        @Schema(description = "Technician identifier (UUID)", example = "9b2e4c71-3f5a-4d8b-b6c0-1e7a2d9f4c35")
        String technicianId,
        @Schema(description = "Whether the technician accepted the job", example = "true")
        boolean accepted
) {
}
