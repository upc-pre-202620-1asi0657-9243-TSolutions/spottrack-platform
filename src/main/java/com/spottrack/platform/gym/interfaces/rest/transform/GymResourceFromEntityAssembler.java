package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.interfaces.rest.resources.GymResource;

public class GymResourceFromEntityAssembler {
    public static GymResource toResourceFromEntity(Gym entity) {
        return new GymResource(entity.getId().uuid(), entity.getName(), entity.getAdminUserId());
    }
}
