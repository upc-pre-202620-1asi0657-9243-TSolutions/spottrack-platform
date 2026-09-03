package com.spottrack.platform.monitoring.interfaces.events;

/**
 * Published when a session tracker ends and is tied to a booked reservation.
 * The {@code reservation} bounded context listens for this to end the
 * reservation too, so usage ending on the monitoring side (e.g. inactivity
 * timeout, admin ending the session) doesn't leave a stale active reservation.
 */
public record SessionTrackerEndedIntegrationEvent(String sessionTrackerId, String reservationId) {
}
