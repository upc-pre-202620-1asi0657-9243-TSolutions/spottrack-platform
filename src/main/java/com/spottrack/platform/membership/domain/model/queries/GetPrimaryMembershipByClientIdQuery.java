package com.spottrack.platform.membership.domain.model.queries;

public record GetPrimaryMembershipByClientIdQuery(Long clientId) {
    public GetPrimaryMembershipByClientIdQuery {
        if (clientId == null) throw new IllegalArgumentException("membership.error.clientId.notNull");
    }
}
