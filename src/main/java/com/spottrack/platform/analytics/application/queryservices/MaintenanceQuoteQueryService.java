package com.spottrack.platform.analytics.application.queryservices;

import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.domain.model.queries.GetAllMaintenanceQuotesQuery;

import java.util.List;

public interface MaintenanceQuoteQueryService {
    List<MaintenanceQuote> handle(GetAllMaintenanceQuotesQuery query);
}
