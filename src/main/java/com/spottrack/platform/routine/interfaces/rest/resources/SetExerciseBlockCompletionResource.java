package com.spottrack.platform.routine.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "SetExerciseBlockCompletionRequest",
        description = "Request payload to mark an exercise block as completed or not for a routine session",
        example = "{\"completed\": true}"
)
public record SetExerciseBlockCompletionResource(

        @NotNull
        @Schema(description = "Whether the exercise block is completed", example = "true")
        Boolean completed
) {
}
