package com.spottrack.platform.monitoring.domain.model.events;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;

import java.time.LocalTime;

public record SessionTimeCalculatedEvent(SessionTrackerId sessionTrackerId, EquipmentId equipmentId, LocalTime trueActivity) {
}
