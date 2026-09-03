package com.spottrack.platform.routine.interfaces.rest.resources;

import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "AddExerciseBlockRequest",
        description = "Request payload for adding an exercise block to a routine",
        example = "{\"routineId\": 1, \"exerciseName\": \"Push-up\", \"exerciseType\": \"STRENGTH\", \"order\": 1, \"sets\": 3, \"reps\": 12}"
)
public record AddExerciseBlockResource(

        @NotNull
        @Schema(description = "Routine identifier", example = "1")
        Long routineId,

        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Name of the exercise", example = "Push-up", minLength = 1, maxLength = 100)
        String exerciseName,

        @NotNull
        @Schema(description = "Type of exercise", example = "STRENGTH", allowableValues = {"CARDIO", "STRENGTH", "FLEXIBILITY"})
        ExerciseType exerciseType,

        @NotNull
        @Schema(description = "Order of the exercise block within the routine", example = "1")
        int order,

        @Schema(description = "Number of sets prescribed for this exercise", example = "3")
        int sets,

        @Schema(description = "Number of repetitions per set prescribed for this exercise", example = "12")
        int reps
) {
}
