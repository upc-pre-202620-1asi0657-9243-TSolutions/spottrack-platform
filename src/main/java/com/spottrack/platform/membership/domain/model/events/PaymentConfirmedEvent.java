package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentPurpose;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;

import java.util.UUID;

public record PaymentConfirmedEvent(
        Long id,
        UUID paymentId,
        Long userId,
        UUID pendingRegistrationId,
        UUID membershipId,
        MembershipTier membershipTier,
        Money amount,
        String gatewayTransactionId,
        PaymentPurpose paymentPurpose
) {
    public static PaymentConfirmedEvent from(Payment payment) {
        return new PaymentConfirmedEvent(
                payment.getId(),
                payment.getPaymentId().uuid(),
                payment.getUserId(),
                payment.getPendingRegistrationId(),
                payment.getMembershipId(),
                payment.getMembershipTier(),
                payment.getAmount(),
                payment.getGatewayTransactionId(),
                payment.getPaymentPurpose()
        );
    }
}
