package com.spottrack.platform.maintenance.interfaces.rest.transform;

import com.spottrack.platform.maintenance.domain.model.commands.RequestMaintenance;
import com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.maintenance.interfaces.rest.resources.RequestMaintenanceResource;

public class RequestMaintenanceCommandFromResourceAssembler {

    public static RequestMaintenance toCommandFromResource(RequestMaintenanceResource resource) {
        return new RequestMaintenance(new EquipmentId(resource.equipmentId()), resource.requestedBy(), resource.description());
    }
}
