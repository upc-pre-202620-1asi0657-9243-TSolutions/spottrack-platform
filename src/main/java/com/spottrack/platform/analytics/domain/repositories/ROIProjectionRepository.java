package com.spottrack.platform.analytics.domain.repositories;

import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;
import java.util.List;
import java.util.Optional;

public interface ROIProjectionRepository {
    ROIProjection save(ROIProjection roiProjection);
    Optional<ROIProjection> findByRoiProjectionId(ROIProjectionId roiProjectionId);
    Optional<ROIProjection> findById(Long id);
    List<ROIProjection> findAll();
}
