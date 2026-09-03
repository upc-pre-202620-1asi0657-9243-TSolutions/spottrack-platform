package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource for updating the authenticated user's notification preferences")
public record NotificationPreferencesResource(
        @Schema(description = "Notify on critical alerts") boolean notifyOnCritical,
        @Schema(description = "Notify on warning alerts") boolean notifyOnWarning,
        @Schema(description = "Email address to notify, defaults to the account's own address if blank") String notificationEmail
) {
}
