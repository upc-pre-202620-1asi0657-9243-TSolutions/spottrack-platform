package com.spottrack.platform.analytics.domain.model.queries;

import java.time.LocalDate;

/** Cross-branch demand comparison of a gym over the days from..to (both inclusive). */
public record GetRelocationInsightsQuery(String gymId, LocalDate from, LocalDate to) {
    public GetRelocationInsightsQuery {
        if (gymId == null || gymId.isBlank()) {
            throw new IllegalArgumentException("gymId is required");
        }
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
    }
}
