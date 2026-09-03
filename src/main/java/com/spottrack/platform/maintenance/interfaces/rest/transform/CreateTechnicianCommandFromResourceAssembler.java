package com.spottrack.platform.maintenance.interfaces.rest.transform;

import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnician;
import com.spottrack.platform.maintenance.interfaces.rest.resources.CreateTechnicianResource;

public class CreateTechnicianCommandFromResourceAssembler {

    public static CreateTechnician toCommandFromResource(CreateTechnicianResource resource) {
        return new CreateTechnician(resource.name());
    }
}
