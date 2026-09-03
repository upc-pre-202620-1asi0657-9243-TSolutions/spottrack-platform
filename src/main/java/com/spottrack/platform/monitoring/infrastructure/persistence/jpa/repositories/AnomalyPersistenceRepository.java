package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.AnomalyPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnomalyPersistenceRepository extends JpaRepository<AnomalyPersistenceEntity, Long> {
    AnomalyPersistenceEntity save(AnomalyPersistenceEntity entity);
}
