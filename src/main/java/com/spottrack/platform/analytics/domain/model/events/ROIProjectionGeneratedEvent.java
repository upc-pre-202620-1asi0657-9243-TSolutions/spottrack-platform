package com.spottrack.platform.analytics.domain.model.events;

import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;

public record ROIProjectionGeneratedEvent(ROIProjectionId roiProjectionId, Double roiIndex) {
}
