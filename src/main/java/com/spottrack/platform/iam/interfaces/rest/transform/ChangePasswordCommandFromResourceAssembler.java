package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.commands.ChangePasswordCommand;
import com.spottrack.platform.iam.interfaces.rest.resources.ChangePasswordResource;

public class ChangePasswordCommandFromResourceAssembler {

    public static ChangePasswordCommand toCommandFromResource(ChangePasswordResource resource, String username) {
        return new ChangePasswordCommand(username, resource.currentPassword(), resource.newPassword());
    }
}
