package com.spottrack.platform.analytics.domain.model.valueobjects;

public record ROIProjectionId(Long value) {
    public ROIProjectionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("RoiProjectionId must be a positive number");
        }
    }
}