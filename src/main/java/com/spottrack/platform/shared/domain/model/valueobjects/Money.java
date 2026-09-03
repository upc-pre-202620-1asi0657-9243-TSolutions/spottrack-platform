package com.spottrack.platform.shared.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import java.math.BigDecimal;

@Embeddable
public record Money(
        @Getter
        BigDecimal amount,
        @Getter
        String currency) {
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("money.error.amount.null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("money.error.amount.negative");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("money.error.currency.blank");
        }
        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("money.error.currency.invalidFormat");
        }
    }
}
