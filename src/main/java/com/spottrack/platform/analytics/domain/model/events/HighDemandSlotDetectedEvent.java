package com.spottrack.platform.analytics.domain.model.events;

import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;

public record HighDemandSlotDetectedEvent(ROIProjectionId roiProjectionId, String slotInfo) {
}
