package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentPurpose;

import java.util.UUID;

public record PaymentFailedEvent(
        Long id,
        UUID paymentId,
        Long userId,
        UUID pendingRegistrationId,
        UUID membershipId,
        PaymentPurpose paymentPurpose
) {
    public static PaymentFailedEvent from(Payment payment) {
        return new PaymentFailedEvent(
                payment.getId(),
                payment.getPaymentId().uuid(),
                payment.getUserId(),
                payment.getPendingRegistrationId(),
                payment.getMembershipId(),
                payment.getPaymentPurpose()
        );
    }
}
