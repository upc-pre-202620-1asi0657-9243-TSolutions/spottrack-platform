package com.spottrack.platform.monitoring.interfaces.rest.resources;

import java.time.LocalTime;

public record CreateSessionTrackerResource(String sessionTrackerId, String equipmentId, String reservationId, boolean sessionIsActive, boolean sessionIsInactive, LocalTime seconds, LocalTime continuousActivity) {

}
