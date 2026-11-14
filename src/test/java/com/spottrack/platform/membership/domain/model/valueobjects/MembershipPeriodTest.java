package com.spottrack.platform.membership.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MembershipPeriodTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    void expiredPeriodCanBeRebuiltFromStoredDates() {
        var start = TODAY.minusDays(60);
        var end = TODAY.minusDays(30);

        var period = new MembershipPeriod(start, end);

        assertEquals(end, period.endDate());
    }

    @Test
    void newPeriodThatAlreadyEndedIsRejected() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> MembershipPeriod.startingNew(TODAY.minusDays(60), TODAY.minusDays(30)));

        assertEquals("membership.error.membershipPeriod.invalid.endDateBeforeCurrentDate", error.getMessage());
    }

    @Test
    void newPeriodEndingInTheFutureIsAccepted() {
        var period = MembershipPeriod.startingNew(TODAY, TODAY.plusDays(30));

        assertEquals(TODAY.plusDays(30), period.endDate());
    }

    @Test
    void endBeforeStartIsRejectedEvenWhenRebuilt() {
        assertThrows(IllegalArgumentException.class,
                () -> new MembershipPeriod(TODAY.minusDays(10), TODAY.minusDays(20)));
    }

    @Test
    void sameStartAndEndIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MembershipPeriod(TODAY, TODAY));
    }

    @Test
    void periodLongerThanFiveYearsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new MembershipPeriod(TODAY, TODAY.plusYears(5).plusDays(1)));
    }
}
