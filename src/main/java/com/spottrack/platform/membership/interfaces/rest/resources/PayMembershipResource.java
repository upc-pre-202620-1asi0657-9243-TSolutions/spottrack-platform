package com.spottrack.platform.membership.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to pay for a membership plan")
public record PayMembershipResource(
        @Schema(description = "Identifier of the user who will own the membership", example = "12")
        @NotNull Long userId,
        @Schema(description = "Plan to pay for", example = "MID", allowableValues = {"BASIC", "MID", "PLATINUM"})
        @NotBlank String membershipTier
) {}
