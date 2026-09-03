package com.spottrack.platform.monitoring.domain.model.valueobjects;

import java.time.LocalTime;

public record UsageActivity(
        LocalTime continuousActivity, LocalTime seconds
) {
    public UsageActivity {
        if (continuousActivity == null || seconds == null) {
            throw new IllegalArgumentException("Usage activity times must not be null");
        }
    }
}
