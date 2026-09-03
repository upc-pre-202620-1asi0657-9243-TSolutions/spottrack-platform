package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "Resource for registering a new user")
public record SignUpResource(
        @Schema(description = "Username (email) for the new account")
        @NotBlank String username,

        @Schema(description = "Password for the new account")
        @NotBlank String password,

        @Schema(description = "List of role names to assign (e.g. ROLE_CLIENT, ROLE_ADMIN)")
        List<String> roles
) {
}
