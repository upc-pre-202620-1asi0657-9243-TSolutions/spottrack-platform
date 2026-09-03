package com.spottrack.platform.membership.domain.model.queries;

import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;

public record GetPaymentByIdQuery(PaymentId paymentId) {
    public GetPaymentByIdQuery {
        if (paymentId == null) throw new IllegalArgumentException("membership.error.paymentId.notNull");
    }
}
