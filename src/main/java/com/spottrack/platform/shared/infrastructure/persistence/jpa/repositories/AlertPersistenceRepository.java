package com.spottrack.platform.shared.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertPersistenceRepository extends JpaRepository<AlertPersistenceEntity, Long> {
    List<AlertPersistenceEntity> findAllByAdminUserIdAndResolvedFalse(Long adminUserId);
}
