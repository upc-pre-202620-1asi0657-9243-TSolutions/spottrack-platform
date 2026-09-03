package com.spottrack.platform.membership.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record ResubscribeMembershipResource(
        @NotBlank String membershipTier
) {}
