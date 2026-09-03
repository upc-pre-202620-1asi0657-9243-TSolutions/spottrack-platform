package com.spottrack.platform.analytics.interfaces.rest.transform;

import com.spottrack.platform.analytics.domain.model.aggregates.ActivityReport;
import com.spottrack.platform.analytics.interfaces.rest.resources.ActivityReportResource;

public class ActivityReportResourceFromEntityAssembler {

    public static ActivityReportResource toResourceFromEntity(ActivityReport entity) {
        return new ActivityReportResource(
                entity.getId(),
                entity.getActivityReportId() != null ? entity.getActivityReportId().value() : null,
                entity.getEquipmentId(),
                entity.getTotalUsageTime(),
                entity.getDowntimeCost(),
                entity.getPercentageComparison()
        );
    }
}
