package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenanceJobPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaintenanceJobJpaRepository extends JpaRepository<MaintenanceJobPersistenceEntity, Long> {
    Optional<MaintenanceJobPersistenceEntity> findByJobId(String jobId);
}
