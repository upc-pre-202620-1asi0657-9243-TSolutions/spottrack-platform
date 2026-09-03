package com.spottrack.platform.membership.domain.model.valueobjects;

import java.util.UUID;

public record PaymentId(UUID uuid) {
    public PaymentId() { this(UUID.randomUUID()); }

    public PaymentId {
        if (uuid == null) throw new IllegalArgumentException("membership.error.paymentId.notNull");
    }
}
