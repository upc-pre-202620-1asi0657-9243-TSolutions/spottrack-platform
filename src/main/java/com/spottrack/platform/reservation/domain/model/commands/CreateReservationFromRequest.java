package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;

/**
 * Policy-driven counterpart to InitiateExpressReservation: creates the Reservation
 * that backs a submitted ReservationRequest, so StartReservationTimer has something
 * to operate on later (mirrors the express flow, which creates its ReservationRequest
 * in reverse via ExpressReservationInitiatedEventHandler).
 *
 * No explicit time window is collected at request-submission time, so the
 * Reservation gets a full-day placeholder TimeInterval — the actual usage
 * window is governed by the timer (durationMinutes), not this interval.
 */
public record CreateReservationFromRequest(ClientId clientId, EquipmentId equipmentId) {
}
