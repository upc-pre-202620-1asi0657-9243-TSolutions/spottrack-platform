package com.spottrack.platform.routine.domain.model.commands;

public record SetExerciseBlockCompletionCommand(Long sessionId, Long exerciseBlockId, boolean completed) {
}
