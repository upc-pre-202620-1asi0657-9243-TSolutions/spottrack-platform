package com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.ZonePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZonePersistenceRepository extends JpaRepository<ZonePersistenceEntity, Long> {
    Optional<ZonePersistenceEntity> findByZoneId(String zoneId);
    Optional<ZonePersistenceEntity> findByName(String zoneName);
    List<ZonePersistenceEntity> findByBranchId(String branchId);
    List<ZonePersistenceEntity> findByBranchIdIn(List<String> branchIds);
}
