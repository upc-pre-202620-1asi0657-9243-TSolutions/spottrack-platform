package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.TechnicianPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnicianJpaRepository extends JpaRepository<TechnicianPersistenceEntity, Long> {
    Optional<TechnicianPersistenceEntity> findByTechnicianId(String technicianId);
    boolean existsByTechnicianId(String technicianId);
    Optional<TechnicianPersistenceEntity> findByUserId(Long userId);
    List<TechnicianPersistenceEntity> findByAdminUserIdOrAdminUserIdIsNull(Long adminUserId);
}
