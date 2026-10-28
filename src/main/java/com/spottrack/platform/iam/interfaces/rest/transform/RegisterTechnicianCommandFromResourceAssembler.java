package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.commands.RegisterTechnicianCommand;
import com.spottrack.platform.iam.interfaces.rest.resources.RegisterTechnicianResource;

public class RegisterTechnicianCommandFromResourceAssembler {

    public static RegisterTechnicianCommand toCommandFromResource(RegisterTechnicianResource resource, Long adminUserId) {
        return new RegisterTechnicianCommand(
                resource.email().trim(),
                resource.password(),
                resource.firstName().trim(),
                resource.lastName().trim(),
                adminUserId);
    }
}
