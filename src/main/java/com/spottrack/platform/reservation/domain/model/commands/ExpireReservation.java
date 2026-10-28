package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;

/**
 * Command: expire an express reservation whose hold elapsed without the client checking in.
 */
public record ExpireReservation(ReservationId reservationId) {
}
