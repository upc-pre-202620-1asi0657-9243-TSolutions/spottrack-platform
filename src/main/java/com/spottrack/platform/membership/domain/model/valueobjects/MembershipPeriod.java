package com.spottrack.platform.membership.domain.model.valueobjects;

import java.time.LocalDate;

/**
 * The canonical constructor checks the period's shape only, so periods that already
 * ended (e.g. expired memberships loaded from the database) can still be rebuilt.
 * Periods coming from a command must use {@link #startingNew}.
 */
public record MembershipPeriod(LocalDate startDate, LocalDate endDate) {

    /** A period requested now: it must not be over already. */
    public static MembershipPeriod startingNew(LocalDate startDate, LocalDate endDate) {
        var period = new MembershipPeriod(startDate, endDate);
        if (endDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("membership.error.membershipPeriod.invalid.endDateBeforeCurrentDate");
        }
        return period;
    }

    public MembershipPeriod {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("membership.error.membershipPeriod.notNull");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("membership.error.membershipPeriod.invalid.endDateBeforeStartDate");
        }

        if (endDate.isEqual(startDate)){
            throw new IllegalArgumentException("membership.error.membershipPeriod.invalid.endDateEqualsStartDate");
        }

        if (endDate.isAfter(startDate.plusYears(5))){
            throw new IllegalArgumentException("membership.error.membershipPeriod.invalid.durationTooLong");
        }

    }
}
