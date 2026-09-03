package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.reservation.domain.model.valueobjects.TimeInterval;

/**
 * Command: fast-track reservation that skips the ReservationRequest flow.
 * startedAt is set by the aggregate (LocalDateTime.now()).
 * timerExpiry is set later via StartReservationTimer.
 */
public record InitiateExpressReservation(ClientId clientId, EquipmentId equipmentId, TimeInterval timeInterval) {
}
