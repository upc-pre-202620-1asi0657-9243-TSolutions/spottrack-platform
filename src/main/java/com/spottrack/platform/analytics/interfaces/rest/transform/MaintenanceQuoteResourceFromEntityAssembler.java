package com.spottrack.platform.analytics.interfaces.rest.transform;

import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.interfaces.rest.resources.MaintenanceQuoteResource;

public class MaintenanceQuoteResourceFromEntityAssembler {
    public static MaintenanceQuoteResource toResourceFromEntity(MaintenanceQuote entity) {
        return new MaintenanceQuoteResource(
                entity.getId(),
                entity.getMaintenanceQuoteId() != null ? entity.getMaintenanceQuoteId().value() : null,
                entity.getEquipmentId(),
                entity.getCorrectiveActionsCost(),
                entity.getSparePartsCost(),
                entity.getPreventiveCost(),
                entity.getTotalMaintenanceCost()
        );
    }
}
