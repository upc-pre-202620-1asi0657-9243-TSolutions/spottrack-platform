package com.spottrack.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities.ROIProjectionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaROIProjectionRepository extends JpaRepository<ROIProjectionPersistenceEntity, Long> {
    Optional<ROIProjectionPersistenceEntity> findByRoiProjectionId(ROIProjectionId roiProjectionId);
}
