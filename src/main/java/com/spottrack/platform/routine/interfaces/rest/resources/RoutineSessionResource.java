package com.spottrack.platform.routine.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;

@Schema(
        name = "RoutineSessionResponse",
        description = "Routine session information response"
)
public record RoutineSessionResource(

        @Schema(description = "Routine session unique identifier", example = "1")
        Long id,

        @Schema(description = "Routine identifier", example = "1")
        Long routineId,

        @Schema(description = "Client identifier", example = "1")
        Long clientId,

        @Schema(description = "Session status", example = "STARTED")
        String status,

        @Schema(description = "Session start timestamp")
        Date startedAt,

        @Schema(description = "IDs of the exercise blocks marked as completed in this session")
        List<Long> completedExerciseBlockIds
) {}
