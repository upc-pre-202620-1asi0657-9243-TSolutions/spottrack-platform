package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Technician in an administrator's roster")
public record TechnicianResource(
        @Schema(description = "Technician identifier (UUID)", example = "9b2e4c71-3f5a-4d8b-b6c0-1e7a2d9f4c35")
        String id,
        @Schema(description = "Full name of the technician", example = "Carlos Ramirez")
        String name
) {}
