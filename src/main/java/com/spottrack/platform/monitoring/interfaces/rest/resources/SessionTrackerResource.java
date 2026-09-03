package com.spottrack.platform.monitoring.interfaces.rest.resources;

import java.time.LocalTime;

public record SessionTrackerResource(
        String sessionTrackerId,
        String equipmentId,
        String equipmentName,
        String reservationId,
        Long clientId,
        String clientName,
        LocalTime continouosActivitiy,
        LocalTime seconds,
        boolean sessionIsActive,
        boolean sessionIsInactive,
        LocalTime calculatedTrueActivity
) {
}
