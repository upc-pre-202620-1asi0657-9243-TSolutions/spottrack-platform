package com.spottrack.platform.profiles.interfaces.rest.transform;

import com.spottrack.platform.profiles.domain.model.commands.CreateAdminCommand;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.interfaces.rest.resources.CreateAdminResource;

public class CreateAdminCommandFromResourceAssembler {

    public static CreateAdminCommand toCommandFromResource(CreateAdminResource resource, Long userId) {
        return new CreateAdminCommand(
                userId,
                new EmailAddress(resource.email()));
    }
}
