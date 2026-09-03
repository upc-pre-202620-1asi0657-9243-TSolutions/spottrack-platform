package com.spottrack.platform.membership.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record DowngradeMembershipPlanResource(
        @NotBlank String newMembershipTier
) {}
