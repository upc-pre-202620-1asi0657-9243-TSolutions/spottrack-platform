package com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchPersistenceRepository extends JpaRepository<BranchPersistenceEntity, Long> {
    Optional<BranchPersistenceEntity> findByBranchId(String branchId);
    Optional<BranchPersistenceEntity> findByName(String name);
    List<BranchPersistenceEntity> findAllByName(String name);
    List<BranchPersistenceEntity> findByGymId(String gymId);
    int countByGymId(String gymId);
}
