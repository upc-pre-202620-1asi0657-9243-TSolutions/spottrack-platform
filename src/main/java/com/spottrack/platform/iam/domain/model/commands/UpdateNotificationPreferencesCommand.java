package com.spottrack.platform.iam.domain.model.commands;

public record UpdateNotificationPreferencesCommand(
        String username,
        boolean notifyOnCritical,
        boolean notifyOnWarning,
        String notificationEmail
) {
}
