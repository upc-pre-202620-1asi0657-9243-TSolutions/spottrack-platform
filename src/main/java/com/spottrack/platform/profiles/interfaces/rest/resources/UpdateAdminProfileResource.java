package com.spottrack.platform.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "UpdateAdminProfileRequest",
        description = "Request payload for updating an admin's personal profile data",
        example = "{\"firstName\": \"John\", \"lastName\": \"Smith\", \"phoneNumber\": \"123456789\", \"dni\": \"87654321\"}"
)
public record UpdateAdminProfileResource(

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Admin first name", example = "John")
        String firstName,

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Admin last name", example = "Smith")
        String lastName,

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Admin phone number (7-15 digits)", example = "123456789")
        String phoneNumber,

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Admin DNI (8 digits)", example = "87654321")
        String dni
) {
}
