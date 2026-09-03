package com.spottrack.platform.routine.domain.model.queries;

import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;

public record GetAllRoutineSessionsByClientIdQuery(ClientId clientId) {}
