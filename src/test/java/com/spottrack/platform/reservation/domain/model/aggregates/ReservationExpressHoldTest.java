package com.spottrack.platform.reservation.domain.model.aggregates;

import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.events.ExpressReservationInitiatedEvent;
import com.spottrack.platform.reservation.domain.model.events.ReservationExpiredEvent;
import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationExpressHoldTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 27, 18, 30, 15);
    private static final int HOLD_MINUTES = 10;

    private static InitiateExpressReservation command(Time start, Time end) {
        return new InitiateExpressReservation(new ClientId(5L), new EquipmentId("eq-1"), start, end);
    }

    private static Reservation hold() {
        return new Reservation(command(null, null), NOW, HOLD_MINUTES);
    }

    @Test
    void expressReservationHoldsForConfiguredMinutes() {
        var reservation = hold();

        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(NOW, reservation.getStartedAt());
        assertEquals(NOW.plusMinutes(10), reservation.getHoldExpiresAt());
        assertNull(reservation.getTimerExpiry());
    }

    @Test
    void sentTimesAreKeptAndDoNotChangeTheHold() {
        var reservation = new Reservation(command(Time.valueOf("20:00:00"), Time.valueOf("21:30:00")), NOW, HOLD_MINUTES);

        assertEquals(Time.valueOf("20:00:00"), reservation.getTimeInterval().startTime());
        assertEquals(Time.valueOf("21:30:00"), reservation.getTimeInterval().endTime());
        assertEquals(NOW.plusMinutes(10), reservation.getHoldExpiresAt());
    }

    @Test
    void missingTimesAreDerivedFromNowAndHold() {
        var reservation = hold();

        assertEquals(Time.valueOf("18:30:00"), reservation.getTimeInterval().startTime());
        assertEquals(Time.valueOf("18:40:00"), reservation.getTimeInterval().endTime());
    }

    @Test
    void onlyTheMissingBoundIsDerived() {
        var onlyStart = new Reservation(command(Time.valueOf("19:00:00"), null), NOW, HOLD_MINUTES);
        var onlyEnd = new Reservation(command(null, Time.valueOf("19:45:00")), NOW, HOLD_MINUTES);

        assertEquals(Time.valueOf("19:00:00"), onlyStart.getTimeInterval().startTime());
        assertEquals(Time.valueOf("19:10:00"), onlyStart.getTimeInterval().endTime());
        assertEquals(Time.valueOf("18:30:00"), onlyEnd.getTimeInterval().startTime());
        assertEquals(Time.valueOf("19:45:00"), onlyEnd.getTimeInterval().endTime());
    }

    @Test
    void derivedTimesNeverCrossTheEndOfTheDay() {
        var lateNight = LocalDateTime.of(2026, 10, 27, 23, 57, 0);

        var reservation = new Reservation(command(null, null), lateNight, HOLD_MINUTES);

        assertEquals(Time.valueOf("23:57:00"), reservation.getTimeInterval().startTime());
        assertEquals(Time.valueOf("23:59:00"), reservation.getTimeInterval().endTime());
        assertEquals(lateNight.plusMinutes(10), reservation.getHoldExpiresAt());
    }

    @Test
    void invalidSentIntervalIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation(command(Time.valueOf("21:00:00"), Time.valueOf("20:00:00")), NOW, HOLD_MINUTES));
    }

    @Test
    void holdMinutesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(command(null, null), NOW, 0));
    }

    @Test
    void initiatedEventIsRaisedForExpressReservations() {
        var reservation = hold();

        reservation.onInitiated();

        assertInstanceOf(ExpressReservationInitiatedEvent.class, reservation.domainEvents().iterator().next());
    }

    @Test
    void holdIsExpiredExactlyAtAndAfterTheDeadline() {
        var reservation = hold();

        assertFalse(reservation.isHoldExpiredAt(NOW.plusMinutes(10).minusSeconds(1)));
        assertTrue(reservation.isHoldExpiredAt(NOW.plusMinutes(10)));
        assertTrue(reservation.isHoldExpiredAt(NOW.plusMinutes(11)));
    }

    @Test
    void expireMovesAnElapsedHoldToExpiredAndRaisesEvent() {
        var reservation = hold();

        reservation.expire(NOW.plusMinutes(10));

        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        var event = assertInstanceOf(ReservationExpiredEvent.class, reservation.domainEvents().iterator().next());
        assertEquals("eq-1", event.equipmentId());
        assertEquals(5L, event.clientId());
    }

    @Test
    void expireIsRejectedBeforeTheHoldElapses() {
        var reservation = hold();

        assertThrows(IllegalStateException.class, () -> reservation.expire(NOW.plusMinutes(9)));
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
    }

    @Test
    void expireIsRejectedOnceTheClientCheckedIn() {
        var reservation = hold();
        reservation.startTimer(45, NOW.plusMinutes(3));

        assertThrows(IllegalStateException.class, () -> reservation.expire(NOW.plusMinutes(11)));
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
    }

    @Test
    void expireIsRejectedForNonActiveReservations() {
        var reservation = hold();
        reservation.cancel();

        assertThrows(IllegalStateException.class, () -> reservation.expire(NOW.plusMinutes(11)));
    }

    @Test
    void checkInWithinTheHoldStartsTheTimer() {
        var reservation = hold();
        var checkIn = NOW.plusMinutes(4);

        reservation.startTimer(45, checkIn);

        assertEquals(checkIn.plusMinutes(45), reservation.getTimerExpiry());
        assertFalse(reservation.isHoldExpiredAt(NOW.plusMinutes(20)));
    }

    @Test
    void checkInAfterTheHoldElapsedIsRejected() {
        var reservation = hold();

        assertThrows(IllegalStateException.class, () -> reservation.startTimer(45, NOW.plusMinutes(10)));
        assertNull(reservation.getTimerExpiry());
    }
}
