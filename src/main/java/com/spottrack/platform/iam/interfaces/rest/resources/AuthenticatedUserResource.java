package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated user resource with JWT token")
public record AuthenticatedUserResource(
        @Schema(description = "User identifier") Long id,
        @Schema(description = "Username (email)") String username,
        @Schema(description = "JWT bearer token") String token
) {
}
