package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;

/**
 * Command: the client explicitly ends the reservation before the timer expires.
 * Moves the Reservation to ENDED and triggers a notification to the Alerts bounded context.
 * Also triggers equipment release back to AVAILABLE.
 */
public record EndReservation(ReservationId reservationId) {


}
