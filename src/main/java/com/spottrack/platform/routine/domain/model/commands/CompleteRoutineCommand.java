package com.spottrack.platform.routine.domain.model.commands;

import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;

public record CompleteRoutineCommand(Long routineSessionId, ClientId clientId) {}
