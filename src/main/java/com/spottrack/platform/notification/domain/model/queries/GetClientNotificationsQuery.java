package com.spottrack.platform.notification.domain.model.queries;

/**
 * Notifications of the client, newest first; {@code unreadOnly} leaves out the ones already read.
 */
public record GetClientNotificationsQuery(Long clientId, boolean unreadOnly) {
}
