package com.spottrack.platform.profiles.interfaces.rest.transform;

import com.spottrack.platform.profiles.domain.model.commands.UpdateClientProfileCommand;
import com.spottrack.platform.profiles.domain.model.valueobjects.ClientId;
import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.profiles.interfaces.rest.resources.UpdateClientProfileResource;

public class UpdateClientProfileCommandFromResourceAssembler {

    public static UpdateClientProfileCommand toCommandFromResource(Long clientId, UpdateClientProfileResource resource) {
        return new UpdateClientProfileCommand(
                new ClientId(clientId),
                resource.firstName(),
                resource.lastName(),
                new PhoneNumber(resource.phoneNumber()),
                new Dni(resource.dni()));
    }
}
