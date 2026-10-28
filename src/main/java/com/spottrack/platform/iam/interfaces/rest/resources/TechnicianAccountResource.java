package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Technical staff account created by an administrator")
public record TechnicianAccountResource(
        @Schema(description = "User identifier") Long id,
        @Schema(description = "Login email of the technician") String email,
        @Schema(description = "Assigned role names") List<String> roles,
        @Schema(description = "Identifier to use when assigning maintenance tickets to this technician") String technicianId
) {
}
