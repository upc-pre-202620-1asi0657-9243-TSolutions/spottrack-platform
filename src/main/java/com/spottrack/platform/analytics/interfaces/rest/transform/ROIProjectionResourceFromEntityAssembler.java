package com.spottrack.platform.analytics.interfaces.rest.transform;

import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.interfaces.rest.resources.ROIProjectionResource;

public class ROIProjectionResourceFromEntityAssembler {
    public static ROIProjectionResource toResourceFromEntity(ROIProjection entity) {
        return new ROIProjectionResource(
                entity.getId(),
                entity.getRoiProjectionId() != null ? entity.getRoiProjectionId().value() : null,
                entity.getRequestedDowntimeCost(),
                entity.getRequestedEarnings(),
                entity.getRoiIndex(),
                entity.getDemandStatus()
        );
    }
}
