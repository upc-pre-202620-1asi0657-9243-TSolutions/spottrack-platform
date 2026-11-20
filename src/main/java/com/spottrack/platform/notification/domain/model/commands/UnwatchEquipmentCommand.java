package com.spottrack.platform.notification.domain.model.commands;

/**
 * The client turns off the notification bell of an equipment.
 */
public record UnwatchEquipmentCommand(Long clientId, String equipmentId) {
    public UnwatchEquipmentCommand {
        if (clientId == null || clientId <= 0) {
            throw new IllegalArgumentException("Client id must be a positive number");
        }
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id must not be null or blank");
        }
    }
}
