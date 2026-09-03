package com.spottrack.platform.membership.domain.model.events;

import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;

import java.util.UUID;

public record MembershipPayedEvent(
        Long id,
        UUID paymentId,
        Long userId,
        UUID pendingRegistrationId,
        MembershipTier membershipTier
) {
    public static MembershipPayedEvent from(Payment payment) {
        return new MembershipPayedEvent(
                payment.getId(),
                payment.getPaymentId().uuid(),
                payment.getUserId(),
                payment.getPendingRegistrationId(),
                payment.getMembershipTier()
        );
    }
}
