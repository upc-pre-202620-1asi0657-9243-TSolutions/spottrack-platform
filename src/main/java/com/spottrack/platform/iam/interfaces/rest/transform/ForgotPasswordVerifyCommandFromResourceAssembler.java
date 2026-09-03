package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.commands.ForgotPasswordVerifyCommand;
import com.spottrack.platform.iam.interfaces.rest.resources.ForgotPasswordVerifyResource;

public class ForgotPasswordVerifyCommandFromResourceAssembler {

    public static ForgotPasswordVerifyCommand toCommandFromResource(ForgotPasswordVerifyResource resource) {
        return new ForgotPasswordVerifyCommand(resource.email(), resource.dni(), resource.newPassword());
    }
}
