package com.spottrack.platform.membership.domain.model.queries;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;

public record GetMembershipByIdQuery(MembershipId membershipId) {
    public GetMembershipByIdQuery {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
    }
}
