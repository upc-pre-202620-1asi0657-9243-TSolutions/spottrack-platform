package com.spottrack.platform.analytics.application.queryservices;

import com.spottrack.platform.analytics.domain.model.queries.GetRelocationInsightsQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.RelocationInsights;

public interface RelocationInsightsQueryService {
    RelocationInsights handle(GetRelocationInsightsQuery query);
}
