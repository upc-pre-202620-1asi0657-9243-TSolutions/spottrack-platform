package com.spottrack.platform.analytics.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.analytics.domain.model.aggregates.ActivityReport;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities.ActivityReportPersistenceEntity;

public final class ActivityReportPersistenceAssembler {

    private ActivityReportPersistenceAssembler() {
    }

    public static ActivityReport toDomainFromPersistence(ActivityReportPersistenceEntity entity) {
        if (entity == null) return null;
        return new ActivityReport(
                entity.getId(),
                entity.getActivityReportId(),
                entity.getEquipmentId(),
                entity.getTotalUsageTime(),
                entity.getDowntimeCost(),
                entity.getPercentageComparison());
    }

    public static ActivityReportPersistenceEntity toPersistenceFromDomain(ActivityReport activityReport) {
        if (activityReport == null) return null;
        var entity = new ActivityReportPersistenceEntity();
        if (activityReport.getId() != null) {
            entity.setId(activityReport.getId());
        }
        entity.setActivityReportId(activityReport.getActivityReportId());
        entity.setEquipmentId(activityReport.getEquipmentId());
        entity.setTotalUsageTime(activityReport.getTotalUsageTime());
        entity.setDowntimeCost(activityReport.getDowntimeCost());
        entity.setPercentageComparison(activityReport.getPercentageComparison());
        return entity;
    }
}
