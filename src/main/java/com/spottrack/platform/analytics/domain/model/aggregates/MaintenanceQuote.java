package com.spottrack.platform.analytics.domain.model.aggregates;

import com.spottrack.platform.analytics.domain.model.events.*;
import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

@Getter
public class MaintenanceQuote extends AbstractDomainAggregateRoot<MaintenanceQuote> {

    @Setter
    private Long id;

    private MaintenanceQuoteId maintenanceQuoteId;

    private String equipmentId;
    private Double correctiveActionsCost;
    private Double sparePartsCost;
    private Double preventiveCost;
    private Double totalMaintenanceCost;

    public MaintenanceQuote() {
    }

    public MaintenanceQuote(MaintenanceQuoteId maintenanceQuoteId, String equipmentId) {
        this.maintenanceQuoteId = maintenanceQuoteId;
        this.equipmentId = equipmentId;
        this.correctiveActionsCost = 0.0;
        this.sparePartsCost = 0.0;
        this.preventiveCost = 0.0;
        this.totalMaintenanceCost = 0.0;
    }

    public MaintenanceQuote(Long id, MaintenanceQuoteId maintenanceQuoteId, String equipmentId, Double correctiveActionsCost,
                             Double sparePartsCost, Double preventiveCost, Double totalMaintenanceCost) {
        this.id = id;
        this.maintenanceQuoteId = maintenanceQuoteId;
        this.equipmentId = equipmentId;
        this.correctiveActionsCost = correctiveActionsCost;
        this.sparePartsCost = sparePartsCost;
        this.preventiveCost = preventiveCost;
        this.totalMaintenanceCost = totalMaintenanceCost;
    }

    public void updateCorrectiveActionsCost(Double cost) {
        this.correctiveActionsCost = cost;
        this.registerDomainEvent(new CostOfTheCorrectiveActionsRequestedEvent(this.maintenanceQuoteId, cost));
        recalculateTotal();
    }

    public void updateSparePartsCost(Double cost) {
        this.sparePartsCost = cost;
        this.registerDomainEvent(new CostOfSparePartsRequestedEvent(this.maintenanceQuoteId, cost));
        recalculateTotal();
    }

    public void updatePreventiveCost(Double cost) {
        this.preventiveCost = cost;
        this.registerDomainEvent(new PreventiveCostRequestedEvent(this.maintenanceQuoteId, cost));
        recalculateTotal();
    }

    /**
     * Explicit admin override of the total — distinct from the automatic
     * recalculation the three component updates already do, e.g. for a
     * vendor-negotiated flat total that doesn't equal the sum of parts.
     */
    public void calculateTotalMaintenanceCost(Double totalCost) {
        this.totalMaintenanceCost = totalCost;
        this.registerDomainEvent(new MaintenanceCostRequestedEvent(this.maintenanceQuoteId, totalCost));
    }

    private void recalculateTotal() {
        this.totalMaintenanceCost = this.correctiveActionsCost + this.sparePartsCost + this.preventiveCost;
    }
}
