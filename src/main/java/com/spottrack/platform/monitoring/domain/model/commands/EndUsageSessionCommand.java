package com.spottrack.platform.monitoring.domain.model.commands;

import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;

public record EndUsageSessionCommand(SessionTrackerId sessionTrackerId) {
}
