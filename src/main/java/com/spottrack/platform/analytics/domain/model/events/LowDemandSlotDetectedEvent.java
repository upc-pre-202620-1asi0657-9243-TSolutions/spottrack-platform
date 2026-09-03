package com.spottrack.platform.analytics.domain.model.events;

import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;

public record LowDemandSlotDetectedEvent(ROIProjectionId roiProjectionId, String slotInfo) {
}
