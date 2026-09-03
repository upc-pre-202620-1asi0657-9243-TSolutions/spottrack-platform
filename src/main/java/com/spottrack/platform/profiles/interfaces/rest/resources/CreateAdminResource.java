package com.spottrack.platform.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "CreateAdminRequest",
        description = "Request payload for creating a new admin profile",
        example = "{\"email\": \"john.smith@example.com\"}"
)
public record CreateAdminResource(

        @NotBlank(message = "{validation.not-blank}")
        @Email(message = "{validation.email}")
        @Schema(description = "Admin email address", example = "john.smith@example.com")
        String email
) {
}
