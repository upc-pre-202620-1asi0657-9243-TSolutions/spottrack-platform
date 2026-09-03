package com.spottrack.platform.monitoring.domain.model.events;

import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;

/**
 * @param reservationId null for walk-up usage (no booked reservation to cascade to)
 */
public record UsageSessionEndedEvent(SessionTrackerId sessionTrackerId, String reservationId) {
}
