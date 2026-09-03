package com.spottrack.platform.gym.interfaces.events;

import java.time.LocalDate;

/**
 * Consumed by the maintenance bounded context to auto-open a RequestMaintenance
 * for the equipment (see maintenance's MaintenanceThresholdReachedEventHandler in the maintenance BC).
 * for the equipment (see maintenance's MaintenanceThresholdReachedIntegrationEventHandler).
 */
public record MaintenanceThresholdReachedIntegrationEvent(String equipmentId, LocalDate threshold) {}
