package com.spottrack.platform.notification.domain.model.commands;

/**
 * Registers a client as affected by a failure of the equipment (e.g. they reported an anomaly on it),
 * so they are notified once the equipment is operational again.
 */
public record RegisterAffectedClientCommand(Long clientId, String equipmentId) {
    public RegisterAffectedClientCommand {
        if (clientId == null || clientId <= 0) {
            throw new IllegalArgumentException("Client id must be a positive number");
        }
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id must not be null or blank");
        }
    }
}
