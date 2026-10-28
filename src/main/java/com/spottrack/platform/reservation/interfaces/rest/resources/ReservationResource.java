package com.spottrack.platform.reservation.interfaces.rest.resources;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;

import java.time.LocalDateTime;

/**
 * Response body for Reservation endpoints.
 * timerExpiry is null until StartReservationTimer is called.
 * holdExpiresAt is the end of the 10-minute express hold (null for non-express reservations).
 */
public record ReservationResource(
        String id,
        Long clientId,
        String equipmentId,
        String status,
        LocalDateTime startedAt,
        LocalDateTime timerExpiry,
        LocalDateTime holdExpiresAt,
        String startTime,   // from timeInterval
        String endTime      // from timeInterval
) {}