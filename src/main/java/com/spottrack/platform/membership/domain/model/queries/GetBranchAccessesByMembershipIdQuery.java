package com.spottrack.platform.membership.domain.model.queries;

public record GetBranchAccessesByMembershipIdQuery(Long membershipId) {
    public GetBranchAccessesByMembershipIdQuery {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
    }
}
