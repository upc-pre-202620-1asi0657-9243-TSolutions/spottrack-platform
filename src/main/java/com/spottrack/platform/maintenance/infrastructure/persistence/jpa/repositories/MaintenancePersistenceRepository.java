package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenancePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaintenancePersistenceRepository extends JpaRepository<MaintenancePersistenceEntity, Long> {
    Optional<MaintenancePersistenceEntity> findByMaintenanceId(String maintenanceId);
}
