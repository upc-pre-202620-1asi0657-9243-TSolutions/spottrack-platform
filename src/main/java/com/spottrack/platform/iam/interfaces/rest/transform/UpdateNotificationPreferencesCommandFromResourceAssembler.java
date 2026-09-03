package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.commands.UpdateNotificationPreferencesCommand;
import com.spottrack.platform.iam.interfaces.rest.resources.NotificationPreferencesResource;

public class UpdateNotificationPreferencesCommandFromResourceAssembler {

    public static UpdateNotificationPreferencesCommand toCommandFromResource(NotificationPreferencesResource resource, String username) {
        return new UpdateNotificationPreferencesCommand(
                username,
                resource.notifyOnCritical(),
                resource.notifyOnWarning(),
                resource.notificationEmail()
        );
    }
}
