package com.spottrack.platform.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "CreateClientRequest",
        description = "Request payload for creating a new client profile",
        example = "{\"userId\": 1, \"email\": \"jane.doe@example.com\"}"
)
public record CreateClientResource(

        @NotNull
        @Schema(description = "IAM user identifier", example = "1")
        Long userId,

        @NotBlank(message = "{validation.not-blank}")
        @Email(message = "{validation.email}")
        @Schema(description = "Client email address", example = "jane.doe@example.com")
        String email
) {
}
