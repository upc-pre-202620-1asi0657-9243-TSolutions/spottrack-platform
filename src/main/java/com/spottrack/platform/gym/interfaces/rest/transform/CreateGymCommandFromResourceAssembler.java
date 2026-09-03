package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.commands.CreateGym;
import com.spottrack.platform.gym.interfaces.rest.resources.CreateGymResource;

public class CreateGymCommandFromResourceAssembler {
    public static CreateGym toCommandFromResource(CreateGymResource resource, Long adminUserId) {
        return new CreateGym(resource.gymName(), adminUserId);
    }
}
