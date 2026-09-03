package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;

/**
 * Command: the client cancels an active reservation before or during use.
 * Moves the Reservation to CANCELLED and should trigger equipment release.
 */
public record CancelReservation(ReservationId reservationId) {

}
