package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;

public record ConfirmPaymentCommand(PaymentId paymentId, String gatewayTransactionId) {
    public ConfirmPaymentCommand {
        if (paymentId == null) throw new IllegalArgumentException("membership.error.paymentId.notNull");
        if (gatewayTransactionId == null || gatewayTransactionId.isBlank())
            throw new IllegalArgumentException("membership.error.gatewayTransactionId.blank");
    }
}
