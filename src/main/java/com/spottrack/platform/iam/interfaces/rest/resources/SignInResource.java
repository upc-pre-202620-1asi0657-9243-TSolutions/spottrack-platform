package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Resource for authenticating an existing user")
public record SignInResource(
        @Schema(description = "Username (email) of the account", example = "jane.doe@spottrack.com")
        @NotBlank String username,

        @Schema(description = "Password of the account", example = "P4ssword!")
        @NotBlank String password
) {
}
