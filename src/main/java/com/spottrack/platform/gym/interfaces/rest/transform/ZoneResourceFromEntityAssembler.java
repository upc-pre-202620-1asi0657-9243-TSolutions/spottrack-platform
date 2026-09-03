package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.entities.Zone;
import com.spottrack.platform.gym.interfaces.rest.resources.ZoneResource;

public class ZoneResourceFromEntityAssembler {
    public static ZoneResource toResourceFromEntity(Zone entity){
        return new ZoneResource(
                entity.getId().uuid(),
                entity.getName(),
                entity.getMaximumOccupancy(),
                entity.getBranchId().uuid()
        );
    }
}
