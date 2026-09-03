package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Role resource representation")
public record RoleResource(
        @Schema(description = "Role identifier") Long id,
        @Schema(description = "Role name") String name
) {
}
