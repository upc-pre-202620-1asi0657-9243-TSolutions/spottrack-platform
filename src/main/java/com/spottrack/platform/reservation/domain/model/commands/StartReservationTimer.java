package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;

/**
 * Command: starts the countdown timer for an active reservation.
 * durationMinutes defines how long the client has to use the equipment.
 * When the timer expires, the policy triggers RequestEquipmentStatusChangeToAvailable.
 */
public record StartReservationTimer(ReservationId reservationId, int durationMinutes) {
    public StartReservationTimer {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be greater than zero");
        }
    }
}
