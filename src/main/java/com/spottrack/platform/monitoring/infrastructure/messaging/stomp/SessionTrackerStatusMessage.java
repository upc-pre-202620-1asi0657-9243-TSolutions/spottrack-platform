package com.spottrack.platform.monitoring.infrastructure.messaging.stomp;

public record SessionTrackerStatusMessage(String sessionTrackerId, String status) {
}
