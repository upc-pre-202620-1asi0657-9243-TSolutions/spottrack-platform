package com.spottrack.platform.membership.domain.model.queries;

public record GetMembershipsByClientIdQuery(Long clientId) {
    public GetMembershipsByClientIdQuery {
        if (clientId == null) throw new IllegalArgumentException("membership.error.clientId.notNull");
    }
}
