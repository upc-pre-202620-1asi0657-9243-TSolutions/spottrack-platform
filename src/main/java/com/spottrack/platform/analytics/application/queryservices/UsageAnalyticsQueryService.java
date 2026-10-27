package com.spottrack.platform.analytics.application.queryservices;

import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;

public interface UsageAnalyticsQueryService {
    UsageHeatmap handle(GetUsageHeatmapQuery query);
}
