package com.spottrack.platform.notification.infrastructure.messaging.stomp;

import java.util.Date;

/**
 * Payload pushed to {@code /user/queue/notifications} when a client notification is created.
 */
public record ClientNotificationMessage(Long id, String equipmentId, String type, String message, Date createdAt) {
}
