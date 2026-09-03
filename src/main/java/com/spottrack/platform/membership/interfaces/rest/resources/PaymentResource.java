package com.spottrack.platform.membership.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResource(
        Long id,
        UUID paymentId,
        Long userId,
        String membershipTier,
        BigDecimal amount,
        String currency,
        String status,
        String gatewayTransactionId
) {}
