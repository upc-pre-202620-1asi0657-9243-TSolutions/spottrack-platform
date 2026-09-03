package com.spottrack.platform.analytics.domain.repositories;

import com.spottrack.platform.analytics.domain.model.aggregates.ActivityReport;
import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;
import java.util.List;
import java.util.Optional;
public interface ActivityReportRepository {
    ActivityReport save(ActivityReport activityReport);
    Optional<ActivityReport> findByActivityReportId(ActivityReportId activityReportId);
    Optional<ActivityReport> findById(Long id);
    Optional<ActivityReport> findByEquipmentId(String equipmentId);
    List<ActivityReport> findAll();
}
