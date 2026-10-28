package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Resource for an administrator to register a technical staff account")
public record RegisterTechnicianResource(
        @Schema(description = "Email of the technician, used as the login username", example = "tecnico@gym.com")
        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "must be a valid email address")
        String email,

        @Schema(description = "Initial password set by the administrator (8 to 72 characters)")
        @NotBlank(message = "must not be blank")
        @Size(min = 8, max = 72, message = "must be between 8 and 72 characters")
        String password,

        @Schema(description = "First name of the technician", example = "Carlos")
        @NotBlank(message = "must not be blank")
        String firstName,

        @Schema(description = "Last name of the technician", example = "Ramirez")
        @NotBlank(message = "must not be blank")
        String lastName
) {
}
