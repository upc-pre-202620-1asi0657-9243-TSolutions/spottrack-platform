package com.spottrack.platform.reservation.interfaces.rest.resources;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;

import java.time.LocalDateTime;

/**
 * Response body for Reservation endpoints.
 * timerExpiry is null until StartReservationTimer is called.
 */
public record ReservationResource(
        String id,
        Long clientId,
        String equipmentId,
        String status,
        LocalDateTime startedAt,
        LocalDateTime timerExpiry,
        String startTime,   // from timeInterval
        String endTime      // from timeInterval
) {}