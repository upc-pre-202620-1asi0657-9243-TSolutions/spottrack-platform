package com.spottrack.platform.analytics.domain.model.aggregates;

import com.spottrack.platform.analytics.domain.model.events.*;
import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ROIProjection extends AbstractDomainAggregateRoot<ROIProjection> {

    @Setter
    private Long id;

    private ROIProjectionId roiProjectionId;

    private Double requestedDowntimeCost;
    private Double requestedEarnings;
    private Double roiIndex;
    private String demandStatus;

    public ROIProjection() {
    }

    public ROIProjection(ROIProjectionId roiProjectionId) {
        this.roiProjectionId = roiProjectionId;
        this.requestedDowntimeCost = 0.0;
        this.requestedEarnings = 0.0;
        this.roiIndex = 0.0;
        this.demandStatus = "NORMAL";
    }

    public ROIProjection(Long id, ROIProjectionId roiProjectionId, Double requestedDowntimeCost,
                          Double requestedEarnings, Double roiIndex, String demandStatus) {
        this.id = id;
        this.roiProjectionId = roiProjectionId;
        this.requestedDowntimeCost = requestedDowntimeCost;
        this.requestedEarnings = requestedEarnings;
        this.roiIndex = roiIndex;
        this.demandStatus = demandStatus;
    }

    public void updateDowntimeCost(Double cost) {
        this.requestedDowntimeCost = cost;
        this.registerDomainEvent(new RequestedDowntimeCostEvent(this.roiProjectionId, cost));
    }

    public void updateEarnings(Double earnings) {
        this.requestedEarnings = earnings;
        this.registerDomainEvent(new RequestedEarningsEvent(this.roiProjectionId, earnings));
    }

    public void generateROIProjection(Double roiIndex) {
        this.roiIndex = roiIndex;
        this.registerDomainEvent(new ROIProjectionGeneratedEvent(this.roiProjectionId, roiIndex));
    }

    public void markAsLowDemand(String slotInfo) {
        this.demandStatus = "LOW";
        this.registerDomainEvent(new LowDemandSlotDetectedEvent(this.roiProjectionId, slotInfo));
    }

    public void markAsHighDemand(String slotInfo) {
        this.demandStatus = "HIGH";
        this.registerDomainEvent(new HighDemandSlotDetectedEvent(this.roiProjectionId, slotInfo));
    }

    public void recommendEquipmentTransfer(String detail) {
        this.registerDomainEvent(new EquipmentTransferRecommendedEvent(this.roiProjectionId, detail));
    }
}
