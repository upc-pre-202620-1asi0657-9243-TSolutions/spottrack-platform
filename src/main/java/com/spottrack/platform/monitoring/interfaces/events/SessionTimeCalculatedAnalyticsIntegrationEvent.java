package com.spottrack.platform.monitoring.interfaces.events;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * startedAt/endedAt place the session on the timeline so Analytics can tell when
 * equipment was used, not just for how long. Both are null when the session never
 * captured motion.
 */
public record SessionTimeCalculatedAnalyticsIntegrationEvent(
        String sessionTrackerId,
        String equipmentId,
        LocalTime trueActivity,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}
