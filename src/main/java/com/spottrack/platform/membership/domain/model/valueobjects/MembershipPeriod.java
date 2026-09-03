package com.spottrack.platform.membership.domain.model.valueobjects;

import java.time.LocalDate;

public record MembershipPeriod(LocalDate startDate, LocalDate endDate) {
    public MembershipPeriod {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("membership.error.membershipPeriod.notNull");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("membership.error.membershipPeriod.invalid.endDateBeforeStartDate");
        }

        if (endDate.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("membership.error.membershipPeriod.invalid.endDateBeforeCurrentDate");
        }

        if (endDate.isEqual(startDate)){
            throw new IllegalArgumentException("membership.error.membershipPeriod.invalid.endDateEqualsStartDate");
        }

        if (endDate.isAfter(startDate.plusYears(5))){
            throw new IllegalArgumentException("membership.error.membershipPeriod.invalid.durationTooLong");
        }

    }
}
