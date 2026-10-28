package com.spottrack.platform.shared.domain.model.commands;

public record ClearAlertCommand(Long alertId, Long adminUserId) {
}
