package com.spottrack.platform.analytics.domain.model.events;

import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;

public record TotalUsageTimeRequestedEvent(ActivityReportId activityReportId, Long totalUsageTime) {
}
