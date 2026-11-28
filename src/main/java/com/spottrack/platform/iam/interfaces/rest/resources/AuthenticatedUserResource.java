package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated user resource with JWT token")
public record AuthenticatedUserResource(
        @Schema(description = "User identifier", example = "42") Long id,
        @Schema(description = "Username (email)", example = "jane.doe@spottrack.com") String username,
        @Schema(description = "Role of the user", example = "ROLE_ADMIN") String role,
        @Schema(description = "JWT bearer token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqYW5lLmRvZUBzcG90dHJhY2suY29tIn0.4pM9c0wq8E1hR2a7Lw6v") String token
) {
}
