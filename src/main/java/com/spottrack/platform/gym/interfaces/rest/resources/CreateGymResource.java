package com.spottrack.platform.gym.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource for creating the administrator's gym")
public record CreateGymResource(
        @Schema(description = "Name of the gym; must not be blank", example = "Spot Gym Miraflores")
        String gymName
) {
}
