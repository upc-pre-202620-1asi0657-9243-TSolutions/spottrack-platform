package com.spottrack.platform.notification.domain.model.commands;

public record MarkNotificationAsReadCommand(Long notificationId, Long clientId) {
    public MarkNotificationAsReadCommand {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("Notification id must be a positive number");
        }
        if (clientId == null || clientId <= 0) {
            throw new IllegalArgumentException("Client id must be a positive number");
        }
    }
}
