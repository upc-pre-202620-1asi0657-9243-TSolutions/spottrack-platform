package com.spottrack.platform.membership.interfaces.rest.resources;

import java.util.UUID;

public record BranchAccessResource(
        Long id,
        UUID branchAccessId,
        Long membershipId,
        Long branchId,
        String status
) {}
