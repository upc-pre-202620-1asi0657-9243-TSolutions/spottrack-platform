package com.spottrack.platform.analytics.domain.model.queries;

import java.time.LocalDate;

/** Same scope as {@link GetUsageHeatmapQuery}, but for the printable report. */
public record GetUsageReportQuery(String gymId, String branchId, LocalDate from, LocalDate to) {
    public GetUsageReportQuery {
        if (gymId == null) {
            throw new IllegalArgumentException("gymId is required");
        }
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
    }
}
