package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Resource for registering a new client account")
public record PublicSignUpResource(
        @Schema(description = "Username (email) for the new account", example = "jane.doe@spottrack.com")
        @NotBlank String username,

        @Schema(description = "Password for the new account", example = "P4ssword!")
        @NotBlank String password
) {
}
