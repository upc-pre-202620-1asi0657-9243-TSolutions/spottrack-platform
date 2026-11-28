package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Technical staff account created by an administrator")
public record TechnicianAccountResource(
        @Schema(description = "User identifier", example = "57") Long id,
        @Schema(description = "Login email of the technician", example = "tecnico@gym.com") String email,
        @ArraySchema(arraySchema = @Schema(description = "Assigned role names"),
                schema = @Schema(example = "ROLE_TECHNICIAN")) List<String> roles,
        @Schema(description = "Identifier to use when assigning maintenance tickets to this technician", example = "9b2e4c71-3f5a-4d8b-b6c0-1e7a2d9f4c35") String technicianId
) {
}
