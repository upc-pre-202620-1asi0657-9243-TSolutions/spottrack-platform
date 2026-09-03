package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Resource for initiating the forgot-password flow")
public record ForgotPasswordResource(
        @Schema(description = "Email address of the account to recover")
        @NotBlank @Email String email
) {
}
