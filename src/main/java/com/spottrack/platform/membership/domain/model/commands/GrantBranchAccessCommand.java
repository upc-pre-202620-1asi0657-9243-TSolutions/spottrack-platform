package com.spottrack.platform.membership.domain.model.commands;

public record GrantBranchAccessCommand(Long membershipId, Long branchId) {
    public GrantBranchAccessCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
        if (branchId == null) throw new IllegalArgumentException("membership.error.branchId.notNull");
    }
}
