package com.spottrack.platform.membership.interfaces.rest.transform;

import com.spottrack.platform.membership.domain.model.commands.ConfirmPaymentCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;
import com.spottrack.platform.membership.interfaces.rest.resources.ConfirmPaymentResource;

import java.util.UUID;

public class ConfirmPaymentCommandFromResourceAssembler {

    public static ConfirmPaymentCommand toCommand(UUID paymentId, ConfirmPaymentResource resource) {
        return new ConfirmPaymentCommand(
                new PaymentId(paymentId),
                resource.gatewayTransactionId()
        );
    }
}
