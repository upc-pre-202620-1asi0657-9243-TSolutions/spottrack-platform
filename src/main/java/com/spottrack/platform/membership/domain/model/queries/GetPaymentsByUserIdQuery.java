package com.spottrack.platform.membership.domain.model.queries;

public record GetPaymentsByUserIdQuery(Long userId) {
    public GetPaymentsByUserIdQuery {
        if (userId == null) throw new IllegalArgumentException("membership.error.userId.notNull");
    }
}
