package com.spottrack.platform.monitoring.domain.model.commands;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.UsageActivity;

/**
 * reservationId is null for walk-up usage (equipment used without a booked
 * reservation) — real usage data still worth tracking.
 */
public record CreateSessionTrackerCommand(
        SessionTrackerId sessionTrackerId,
        EquipmentId equipmentId,
        ReservationId reservationId,
        boolean sessionIsActive,
        boolean sessionIsInactive,
        UsageActivity usageActivity
) {
}
