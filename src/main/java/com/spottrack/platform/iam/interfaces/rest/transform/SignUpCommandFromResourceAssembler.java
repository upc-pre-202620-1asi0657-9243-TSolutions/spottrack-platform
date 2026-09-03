package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.commands.SignUpCommand;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.interfaces.rest.resources.SignUpResource;

import java.util.List;

public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        List<Role> roles = resource.roles() == null ? List.of() :
                resource.roles().stream()
                        .map(Role::toRoleFromName)
                        .toList();
        return new SignUpCommand(resource.username(), resource.password(), roles);
    }
}
