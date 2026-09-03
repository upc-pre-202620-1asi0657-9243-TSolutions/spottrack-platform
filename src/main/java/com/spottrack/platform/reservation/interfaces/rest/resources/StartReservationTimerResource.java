package com.spottrack.platform.reservation.interfaces.rest.resources;

/**
 * Request body for PATCH /api/v1/reservations/{id}/timer
 * durationMinutes: how many minutes the client has to use the equipment.
 */
public record StartReservationTimerResource(int durationMinutes) {
}
