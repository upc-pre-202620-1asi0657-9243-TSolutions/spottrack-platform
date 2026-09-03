package com.spottrack.platform.analytics.application.queryservices;

import com.spottrack.platform.analytics.domain.model.aggregates.ActivityReport;
import com.spottrack.platform.analytics.domain.model.queries.GetAllActivityReportsQuery;

import java.util.List;

public interface ActivityReportQueryService {
    List<ActivityReport> handle(GetAllActivityReportsQuery query);
}
