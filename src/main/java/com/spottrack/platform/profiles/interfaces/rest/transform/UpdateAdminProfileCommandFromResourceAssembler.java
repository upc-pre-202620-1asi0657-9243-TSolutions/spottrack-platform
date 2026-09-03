package com.spottrack.platform.profiles.interfaces.rest.transform;

import com.spottrack.platform.profiles.domain.model.commands.UpdateAdminProfileCommand;
import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;
import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.profiles.interfaces.rest.resources.UpdateAdminProfileResource;

public class UpdateAdminProfileCommandFromResourceAssembler {

    public static UpdateAdminProfileCommand toCommandFromResource(Long adminId, UpdateAdminProfileResource resource) {
        return new UpdateAdminProfileCommand(
                new AdminId(adminId),
                resource.firstName(),
                resource.lastName(),
                new PhoneNumber(resource.phoneNumber()),
                new Dni(resource.dni()));
    }
}
