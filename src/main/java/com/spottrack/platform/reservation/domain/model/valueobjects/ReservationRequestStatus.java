package com.spottrack.platform.reservation.domain.model.valueobjects;

/**
 * Lifecycle states for a ReservationRequest.
 *
 * PENDING              → request created, waiting for equipment confirmation
 * SUBMITTED            → request submitted to equipment bounded context (equipment marked RESERVED)
 * ALTERNATIVE_REQUESTED → client asked for a different piece of equipment
 * AVAILABLE_REQUESTED  → after reservation ends, a signal was sent to mark equipment AVAILABLE again
 */
public enum ReservationRequestStatus {
    PENDING,
    SUBMITTED,
    ALTERNATIVE_REQUESTED,
    AVAILABLE_REQUESTED
}
