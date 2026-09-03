package com.spottrack.platform.reservation.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * Identity value object for the Reservation aggregate.
 * Uses a UUID string so it can be generated in the domain layer without DB sequences.
 */
@Embeddable
public record ReservationId(String uuid) {
    public ReservationId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("reservation.error.reservationId.notBlank");
        }
    }
}
