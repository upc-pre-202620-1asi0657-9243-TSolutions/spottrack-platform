package com.spottrack.platform.routine.domain.model.commands;

import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;

public record SetExerciseBlockCompletionCommand(Long sessionId, ClientId clientId, Long exerciseBlockId, boolean completed) {
}
