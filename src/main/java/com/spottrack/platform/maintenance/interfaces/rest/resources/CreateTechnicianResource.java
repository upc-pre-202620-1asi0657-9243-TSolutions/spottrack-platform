package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource for adding a technician to the administrator's roster")
public record CreateTechnicianResource(
        @Schema(description = "Full name of the technician", example = "Carlos Ramirez")
        String name
) {}
