package com.spottrack.platform.reservation.domain.model.valueobjects;

/**
 * Lifecycle states for a Reservation.
 *
 * ACTIVE     → timer is running, equipment is occupied
 * EXPIRED    → timer ran out; triggers a request to mark equipment AVAILABLE again
 * CANCELLED  → client cancelled before or during the reservation
 * ENDED      → client explicitly ended the session early
 */
public enum ReservationStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED,
    ENDED
}


