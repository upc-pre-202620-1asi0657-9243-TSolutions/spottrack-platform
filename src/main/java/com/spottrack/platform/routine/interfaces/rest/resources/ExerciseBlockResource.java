package com.spottrack.platform.routine.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ExerciseBlockResponse",
        description = "Exercise block information response",
        example = "{\"id\": 1, \"exerciseName\": \"Push-up\", \"exerciseType\": \"STRENGTH\", \"order\": 1, \"sets\": 3, \"reps\": 12}"
)
public record ExerciseBlockResource(

        @Schema(description = "Exercise block unique identifier", example = "1")
        Long id,

        @Schema(description = "Name of the exercise", example = "Push-up")
        String exerciseName,

        @Schema(description = "Type of exercise", example = "STRENGTH")
        String exerciseType,

        @Schema(description = "Order of the exercise block within the routine", example = "1")
        int order,

        @Schema(description = "Number of sets prescribed for this exercise", example = "3")
        int sets,

        @Schema(description = "Number of repetitions per set prescribed for this exercise", example = "12")
        int reps
) {
}
