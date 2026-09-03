package com.spottrack.platform.membership.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

public record GrantBranchAccessResource(
        @NotNull Long membershipId,
        @NotNull Long branchId
) {}
