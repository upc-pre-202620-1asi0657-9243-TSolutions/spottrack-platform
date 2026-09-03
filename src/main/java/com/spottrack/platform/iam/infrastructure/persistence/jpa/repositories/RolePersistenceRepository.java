package com.spottrack.platform.iam.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolePersistenceRepository extends JpaRepository<RolePersistenceEntity, Long> {
    Optional<RolePersistenceEntity> findByName(String name);
    boolean existsByName(String name);
}
