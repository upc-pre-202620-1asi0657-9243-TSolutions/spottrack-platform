package com.spottrack.platform.shared.domain.model.commands;

import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;

public record CreateAlertCommand(Long adminUserId, String equipmentId, AlertSeverity severity, String message) {
    public CreateAlertCommand {
        if (adminUserId == null || adminUserId == 0L) {
            throw new IllegalArgumentException("Admin user id must not be null or zero");
        }
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id cannot be blank");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Severity must not be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }
    }
}
