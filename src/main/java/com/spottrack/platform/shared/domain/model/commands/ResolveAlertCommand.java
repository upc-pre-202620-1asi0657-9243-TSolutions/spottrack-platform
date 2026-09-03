package com.spottrack.platform.shared.domain.model.commands;

public record ResolveAlertCommand(Long alertId, Long adminUserId) {
}
