package com.spottrack.platform.analytics.domain.model.events;

import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;

public record MaintenanceCostRequestedEvent(MaintenanceQuoteId maintenanceQuoteId, Double totalMaintenanceCost) {
}
