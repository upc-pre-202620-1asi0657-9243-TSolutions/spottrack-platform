package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.reservation.domain.model.valueobjects.TimeInterval;

import java.sql.Time;

/**
 * Command: fast-track reservation that skips the ReservationRequest flow.
 * startedAt and holdExpiresAt are set by the aggregate. startTime and endTime are
 * optional: when present they are stored as given, when missing they are derived.
 * timerExpiry is set later via StartReservationTimer.
 */
public record InitiateExpressReservation(ClientId clientId, EquipmentId equipmentId, Time startTime, Time endTime) {

    public InitiateExpressReservation(ClientId clientId, EquipmentId equipmentId, TimeInterval timeInterval) {
        this(clientId, equipmentId, timeInterval.startTime(), timeInterval.endTime());
    }
}
