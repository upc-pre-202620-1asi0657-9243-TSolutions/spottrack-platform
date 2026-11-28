package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Resource for changing the authenticated user's password")
public record ChangePasswordResource(
        @Schema(description = "Current password", example = "OldP4ssword!") @NotBlank String currentPassword,
        @Schema(description = "New password to set", example = "N3wP4ssword!") @NotBlank String newPassword
) {
}
