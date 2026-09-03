package com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities.BusinessProfilePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessProfilePersistenceRepository extends JpaRepository<BusinessProfilePersistenceEntity, Long> {

    Optional<BusinessProfilePersistenceEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
