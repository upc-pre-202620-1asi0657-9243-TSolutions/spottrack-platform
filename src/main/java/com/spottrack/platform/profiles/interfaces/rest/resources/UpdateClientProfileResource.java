package com.spottrack.platform.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "UpdateClientProfileRequest",
        description = "Request payload for updating a client's personal profile data",
        example = "{\"firstName\": \"Jane\", \"lastName\": \"Doe\", \"phoneNumber\": \"987654321\", \"dni\": \"12345678\"}"
)
public record UpdateClientProfileResource(

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Client first name", example = "Jane")
        String firstName,

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Client last name", example = "Doe")
        String lastName,

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Client phone number (7-15 digits)", example = "987654321")
        String phoneNumber,

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Client DNI (8 digits)", example = "12345678")
        String dni
) {
}
