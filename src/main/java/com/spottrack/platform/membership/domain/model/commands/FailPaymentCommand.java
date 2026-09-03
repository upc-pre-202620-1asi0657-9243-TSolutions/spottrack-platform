package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;

public record FailPaymentCommand(PaymentId paymentId) {
    public FailPaymentCommand {
        if (paymentId == null) throw new IllegalArgumentException("membership.error.paymentId.notNull");
    }
}
