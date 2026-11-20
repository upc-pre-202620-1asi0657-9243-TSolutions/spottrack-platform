package com.spottrack.platform.notification.domain.model.commands;

import com.spottrack.platform.notification.domain.model.valueobjects.AvailabilityChange;

/**
 * Applies a change of availability of the equipment to the clients that follow it.
 */
public record NotifyAvailabilityChangeCommand(String equipmentId, AvailabilityChange change) {
    public NotifyAvailabilityChangeCommand {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id must not be null or blank");
        }
        if (change == null) {
            throw new IllegalArgumentException("Availability change must not be null");
        }
    }
}
