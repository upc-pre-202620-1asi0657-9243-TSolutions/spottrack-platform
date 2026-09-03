package com.spottrack.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities.ActivityReportPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaActivityReportRepository extends JpaRepository<ActivityReportPersistenceEntity, Long> {
    Optional<ActivityReportPersistenceEntity> findByActivityReportId(ActivityReportId activityReportId);
    Optional<ActivityReportPersistenceEntity> findByEquipmentId(String equipmentId);
}
