package com.spottrack.platform.reservation.interfaces.rest.resources;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationRequestStatus;

import java.time.LocalDateTime;

/**
 * Response body for ReservationRequest endpoints.
 */
public record ReservationRequestResource(
        String id,
        String clientId,
        String equipmentId,
        ReservationRequestStatus status,
        LocalDateTime requestedAt
) {
}
