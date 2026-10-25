package com.spottrack.platform.analytics.domain.model.commands;

import java.time.LocalDateTime;

public record RecordUsageSessionCommand(String sessionTrackerId, String equipmentId,
                                        LocalDateTime startedAt, LocalDateTime endedAt) {
}
