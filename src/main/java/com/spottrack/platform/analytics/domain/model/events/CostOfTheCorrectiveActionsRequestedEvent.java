package com.spottrack.platform.analytics.domain.model.events;

import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;

public record CostOfTheCorrectiveActionsRequestedEvent(MaintenanceQuoteId maintenanceQuoteId, Double correctiveActionsCost) {
}
