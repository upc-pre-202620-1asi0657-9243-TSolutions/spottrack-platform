package com.spottrack.platform.membership.domain.model.valueobjects;

import com.spottrack.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;

public enum MembershipTier {
    BASIC(69),
    MID(109),
    PLATINUM(189);

    private static final String CURRENCY = "USD";

    private final BigDecimal price;

    MembershipTier(long price) {
        this.price = BigDecimal.valueOf(price);
    }

    public Money toMoney() {
        return new Money(price, CURRENCY);
    }

    public int maxBranches() {
        return switch (this) {
            case BASIC -> 1;
            case MID -> 3;
            case PLATINUM -> Integer.MAX_VALUE;
        };
    }
}
