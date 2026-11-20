package com.spottrack.platform.notification.domain.model.events;

import com.spottrack.platform.notification.domain.model.valueobjects.NotificationType;

import java.util.Date;

/**
 * Published once a client notification has been stored, so it can be delivered in real time.
 */
public record ClientNotificationCreatedEvent(Long notificationId, Long clientId, String equipmentId,
                                             NotificationType type, String message, Date createdAt) {
}
