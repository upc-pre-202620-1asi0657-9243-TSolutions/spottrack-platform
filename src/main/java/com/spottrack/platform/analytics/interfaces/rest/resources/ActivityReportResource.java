package com.spottrack.platform.analytics.interfaces.rest.resources;

public record ActivityReportResource(
        Long id,
        Long activityReportId,
        String equipmentId,
        Long totalUsageTime,
        Long downtimeCost,
        Double percentageComparison
) {
}
