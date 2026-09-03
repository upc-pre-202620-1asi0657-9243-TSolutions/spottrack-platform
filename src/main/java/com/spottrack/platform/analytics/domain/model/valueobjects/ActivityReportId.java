package com.spottrack.platform.analytics.domain.model.valueobjects;

public record ActivityReportId(Long value) {
    public ActivityReportId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ActivityReportId must be a positive number");
        }
    }
}