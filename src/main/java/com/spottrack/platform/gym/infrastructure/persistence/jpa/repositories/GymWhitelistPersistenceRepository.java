package com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.GymWhitelistPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GymWhitelistPersistenceRepository extends JpaRepository<GymWhitelistPersistenceEntity, Long> {
    List<GymWhitelistPersistenceEntity> findByGymId(String gymId);
    boolean existsByGymIdAndDni(String gymId, String dni);
    void deleteByGymIdAndDni(String gymId, String dni);
}
