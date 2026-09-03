package com.spottrack.platform.routine.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RoutineResponse",
        description = "Routine information response",
        example = "{\"id\": 1, \"routineName\": \"Morning Workout\", \"clientId\": 1, \"exerciseBlockCount\": 3}"
)
public record RoutineResource(

        @Schema(description = "Routine unique identifier", example = "1")
        Long id,

        @Schema(description = "Name of the routine", example = "Morning Workout")
        String routineName,

        @Schema(description = "Client identifier", example = "1")
        Long clientId,

        @Schema(description = "Number of exercise blocks in the routine", example = "3")
        int exerciseBlockCount
) {
}
