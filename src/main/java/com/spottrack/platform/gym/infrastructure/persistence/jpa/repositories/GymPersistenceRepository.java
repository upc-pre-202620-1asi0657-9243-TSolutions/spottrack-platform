package com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.GymPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GymPersistenceRepository extends JpaRepository<GymPersistenceEntity, Long> {
    Optional<GymPersistenceEntity> findByGymId(String gymId);
    List<GymPersistenceEntity> findByAdminUserId(Long adminUserId);
}
