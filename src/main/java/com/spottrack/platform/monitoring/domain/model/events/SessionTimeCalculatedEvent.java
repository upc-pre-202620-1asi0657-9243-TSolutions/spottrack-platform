package com.spottrack.platform.monitoring.domain.model.events;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * firstActivityAt/lastActivityAt bound the period the equipment was really in use;
 * both are null when the session never captured motion.
 */
public record SessionTimeCalculatedEvent(SessionTrackerId sessionTrackerId, EquipmentId equipmentId, LocalTime trueActivity,
                                         LocalDateTime firstActivityAt, LocalDateTime lastActivityAt) {
}
