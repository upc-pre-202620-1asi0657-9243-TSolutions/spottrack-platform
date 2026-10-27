package com.spottrack.platform.analytics.domain.model.queries;

import java.time.LocalDate;

/**
 * Usage of a gym, or of one of its branches when branchId is set, over the days
 * from..to (both inclusive).
 */
public record GetUsageHeatmapQuery(String gymId, String branchId, LocalDate from, LocalDate to) {
    public GetUsageHeatmapQuery {
        if (gymId == null && branchId == null) {
            throw new IllegalArgumentException("gymId or branchId is required");
        }
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
    }
}
