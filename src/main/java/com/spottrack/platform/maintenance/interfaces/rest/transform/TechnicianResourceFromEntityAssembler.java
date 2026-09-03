package com.spottrack.platform.maintenance.interfaces.rest.transform;

import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.interfaces.rest.resources.TechnicianResource;

public class TechnicianResourceFromEntityAssembler {

    public static TechnicianResource toResourceFromEntity(Technician entity) {
        return new TechnicianResource(entity.getTechnicianId().uuid(), entity.getName());
    }
}
