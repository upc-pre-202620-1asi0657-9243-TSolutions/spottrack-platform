package com.spottrack.platform.membership.interfaces.rest.transform;

import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.interfaces.rest.resources.PaymentResource;

public class PaymentResourceFromEntityAssembler {

    public static PaymentResource toResourceFromEntity(Payment payment) {
        return new PaymentResource(
                payment.getId(),
                payment.getPaymentId().uuid(),
                payment.getUserId(),
                payment.getMembershipTier().name(),
                payment.getAmount().amount(),
                payment.getAmount().currency(),
                payment.getStatus().name(),
                payment.getGatewayTransactionId()
        );
    }
}
