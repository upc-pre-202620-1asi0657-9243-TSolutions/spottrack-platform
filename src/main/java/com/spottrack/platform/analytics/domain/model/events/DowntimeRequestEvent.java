package com.spottrack.platform.analytics.domain.model.events;

import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;

public record DowntimeRequestEvent(ActivityReportId activityReportId, Long downtimeCost) {
}
