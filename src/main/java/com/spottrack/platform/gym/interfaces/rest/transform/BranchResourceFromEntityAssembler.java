package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.interfaces.rest.resources.BranchResource;

public class BranchResourceFromEntityAssembler {
    public static BranchResource toResourceFromEntity(Branch entity) {
        return new BranchResource(entity.getId().uuid(), entity.getName(), entity.getAddress());
    }
}
