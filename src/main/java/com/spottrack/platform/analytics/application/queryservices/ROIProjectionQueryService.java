package com.spottrack.platform.analytics.application.queryservices;

import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.domain.model.queries.GetAllROIProjectionsQuery;

import java.util.List;

public interface ROIProjectionQueryService {
    List<ROIProjection> handle(GetAllROIProjectionsQuery query);
}
