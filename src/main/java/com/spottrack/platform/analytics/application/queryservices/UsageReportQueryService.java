package com.spottrack.platform.analytics.application.queryservices;

import com.spottrack.platform.analytics.domain.model.queries.GetUsageReportQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageReport;

public interface UsageReportQueryService {
    UsageReport handle(GetUsageReportQuery query);
}
