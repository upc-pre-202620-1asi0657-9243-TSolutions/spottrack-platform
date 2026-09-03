package com.spottrack.platform.analytics.domain.model.events;

import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;

public record PercentageComparisonWithPreviousReportRequestedEvent(ActivityReportId activityReportId, Double percentageComparison) {
}
