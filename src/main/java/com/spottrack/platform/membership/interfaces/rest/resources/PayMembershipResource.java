package com.spottrack.platform.membership.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PayMembershipResource(
        @NotNull Long userId,
        @NotBlank String membershipTier
) {}
