package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.interfaces.rest.resources.SessionTrackerResource;

import java.time.LocalTime;

public class SessionTrackerResourceFromEntity {

    public static SessionTrackerResource toResourceFromEntity(SessionTracker sessionTracker) {
        return toResourceFromEntity(sessionTracker, null, null, null, null);
    }

    public static SessionTrackerResource toResourceFromEntity(
            SessionTracker sessionTracker,
            String equipmentName,
            Long clientId,
            String clientName,
            LocalTime calculatedTrueActivity
    ) {
        return new SessionTrackerResource(
                sessionTracker.getSessionTrackerId().uuid(),
                sessionTracker.getEquipmentId().uuid(),
                equipmentName,
                sessionTracker.getReservationId() != null ? sessionTracker.getReservationId().uuid() : null,
                clientId,
                clientName,
                sessionTracker.getUsageActivity().continuousActivity(),
                sessionTracker.getUsageActivity().seconds(),
                sessionTracker.isSessionIsActive(),
                sessionTracker.isSessionIsInactive(),
                calculatedTrueActivity);
    }
}
