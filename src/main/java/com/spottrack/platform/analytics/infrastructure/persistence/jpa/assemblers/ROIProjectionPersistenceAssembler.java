package com.spottrack.platform.analytics.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities.ROIProjectionPersistenceEntity;

public final class ROIProjectionPersistenceAssembler {

    private ROIProjectionPersistenceAssembler() {
    }

    public static ROIProjection toDomainFromPersistence(ROIProjectionPersistenceEntity entity) {
        if (entity == null) return null;
        return new ROIProjection(
                entity.getId(),
                entity.getRoiProjectionId(),
                entity.getRequestedDowntimeCost(),
                entity.getRequestedEarnings(),
                entity.getRoiIndex(),
                entity.getDemandStatus());
    }

    public static ROIProjectionPersistenceEntity toPersistenceFromDomain(ROIProjection roiProjection) {
        if (roiProjection == null) return null;
        var entity = new ROIProjectionPersistenceEntity();
        if (roiProjection.getId() != null) {
            entity.setId(roiProjection.getId());
        }
        entity.setRoiProjectionId(roiProjection.getRoiProjectionId());
        entity.setRequestedDowntimeCost(roiProjection.getRequestedDowntimeCost());
        entity.setRequestedEarnings(roiProjection.getRequestedEarnings());
        entity.setRoiIndex(roiProjection.getRoiIndex());
        entity.setDemandStatus(roiProjection.getDemandStatus());
        return entity;
    }
}
