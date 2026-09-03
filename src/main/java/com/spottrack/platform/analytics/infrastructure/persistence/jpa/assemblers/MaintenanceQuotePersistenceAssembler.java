package com.spottrack.platform.analytics.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities.MaintenanceQuotePersistenceEntity;

public final class MaintenanceQuotePersistenceAssembler {

    private MaintenanceQuotePersistenceAssembler() {
    }

    public static MaintenanceQuote toDomainFromPersistence(MaintenanceQuotePersistenceEntity entity) {
        if (entity == null) return null;
        return new MaintenanceQuote(
                entity.getId(),
                entity.getMaintenanceQuoteId(),
                entity.getEquipmentId(),
                entity.getCorrectiveActionsCost(),
                entity.getSparePartsCost(),
                entity.getPreventiveCost(),
                entity.getTotalMaintenanceCost());
    }

    public static MaintenanceQuotePersistenceEntity toPersistenceFromDomain(MaintenanceQuote maintenanceQuote) {
        if (maintenanceQuote == null) return null;
        var entity = new MaintenanceQuotePersistenceEntity();
        if (maintenanceQuote.getId() != null) {
            entity.setId(maintenanceQuote.getId());
        }
        entity.setMaintenanceQuoteId(maintenanceQuote.getMaintenanceQuoteId());
        entity.setEquipmentId(maintenanceQuote.getEquipmentId());
        entity.setCorrectiveActionsCost(maintenanceQuote.getCorrectiveActionsCost());
        entity.setSparePartsCost(maintenanceQuote.getSparePartsCost());
        entity.setPreventiveCost(maintenanceQuote.getPreventiveCost());
        entity.setTotalMaintenanceCost(maintenanceQuote.getTotalMaintenanceCost());
        return entity;
    }
}
