package com.spottrack.platform.iam.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPersistenceRepository extends JpaRepository<UserPersistenceEntity, Long> {
    Optional<UserPersistenceEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
