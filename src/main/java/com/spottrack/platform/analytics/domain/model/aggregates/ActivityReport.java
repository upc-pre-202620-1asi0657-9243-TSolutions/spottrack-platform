package com.spottrack.platform.analytics.domain.model.aggregates;

import com.spottrack.platform.analytics.domain.model.commands.RequestActivityAnalysisCommand;
import com.spottrack.platform.analytics.domain.model.events.*;
import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ActivityReport extends AbstractDomainAggregateRoot<ActivityReport> {

    @Setter
    private Long id;

    private ActivityReportId activityReportId;

    private String equipmentId;
    private Long totalUsageTime;
    private Long downtimeCost;
    private Double percentageComparison;

    public ActivityReport() {
    }

    public ActivityReport(RequestActivityAnalysisCommand command, ActivityReportId activityReportId) {
        this.activityReportId = activityReportId;
        this.equipmentId = command.equipmentId();
        this.totalUsageTime = 0L;
        this.downtimeCost = 0L;
        this.percentageComparison = 0.0;
    }

    /**
     * Used by the monitoring event listener to find-or-create a report for
     * an equipment that has never had one before real usage data arrives.
     */
    public ActivityReport(String equipmentId) {
        this.activityReportId = new ActivityReportId(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
        this.equipmentId = equipmentId;
        this.totalUsageTime = 0L;
        this.downtimeCost = 0L;
        this.percentageComparison = 0.0;
    }

    public ActivityReport(Long id, ActivityReportId activityReportId, String equipmentId, Long totalUsageTime, Long downtimeCost, Double percentageComparison) {
        this.id = id;
        this.activityReportId = activityReportId;
        this.equipmentId = equipmentId;
        this.totalUsageTime = totalUsageTime;
        this.downtimeCost = downtimeCost;
        this.percentageComparison = percentageComparison;
    }

    public void updateTotalUsageTime(Long totalUsageTime) {
        this.totalUsageTime = totalUsageTime;
        this.registerDomainEvent(new TotalUsageTimeRequestedEvent(this.activityReportId, totalUsageTime));
    }

    public void updateDowntimeCost(Long downtimeCost) {
        this.downtimeCost = downtimeCost;
        this.registerDomainEvent(new DowntimeRequestEvent(this.activityReportId, downtimeCost));
    }

    public void updatePercentageComparison(Double percentageComparison) {
        this.percentageComparison = percentageComparison;
        this.registerDomainEvent(new PercentageComparisonWithPreviousReportRequestedEvent(this.activityReportId, percentageComparison));
    }
}
