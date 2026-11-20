package com.spottrack.platform.notification.domain.model.commands;

/**
 * The client turns on the notification bell of an equipment that is not available.
 */
public record WatchEquipmentCommand(Long clientId, String equipmentId) {
    public WatchEquipmentCommand {
        if (clientId == null || clientId <= 0) {
            throw new IllegalArgumentException("Client id must be a positive number");
        }
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id must not be null or blank");
        }
    }
}
