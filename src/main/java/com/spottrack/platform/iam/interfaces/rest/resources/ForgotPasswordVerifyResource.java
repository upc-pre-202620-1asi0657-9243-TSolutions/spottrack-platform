package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Resource for verifying identity and setting a new password")
public record ForgotPasswordVerifyResource(
        @Schema(description = "Email address of the account") @NotBlank @Email String email,
        @Schema(description = "DNI registered on the account's profile") @NotBlank String dni,
        @Schema(description = "New password to set") @NotBlank String newPassword
) {
}
