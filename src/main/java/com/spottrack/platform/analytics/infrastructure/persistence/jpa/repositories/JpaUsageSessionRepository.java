package com.spottrack.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities.UsageSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface JpaUsageSessionRepository extends JpaRepository<UsageSessionPersistenceEntity, Long> {
    List<UsageSessionPersistenceEntity> findByEquipmentIdInAndStartedAtLessThanAndEndedAtGreaterThan(
            Collection<String> equipmentIds, LocalDateTime to, LocalDateTime from);
    boolean existsByEquipmentId(String equipmentId);
}
