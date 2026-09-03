package com.spottrack.platform.reservation.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * Identity value object for the ReservationRequest aggregate.
 * Kept separate from ReservationId so both aggregates can evolve independently.
 */
@Embeddable
public record ReservationRequestId(String uuid) {

    public ReservationRequestId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("reservation.error.reservationRequestId.notBlank");
        }
    }
}
