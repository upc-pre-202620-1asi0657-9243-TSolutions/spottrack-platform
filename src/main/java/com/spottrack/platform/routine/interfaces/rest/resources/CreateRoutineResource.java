package com.spottrack.platform.routine.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "CreateRoutineRequest",
        description = "Request payload for creating a new routine",
        example = "{\"routineName\": \"Morning Workout\"}"
)
public record CreateRoutineResource(

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Name of the routine", example = "Morning Workout", minLength = 1, maxLength = 100)
        String routineName
) {
}
