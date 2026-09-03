package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.SessionTrackerPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SessionTrackerPersistenceRepository extends JpaRepository<SessionTrackerPersistenceEntity, Long> {
    Optional<SessionTrackerPersistenceEntity> findBySessionTrackerId(String sessionTrackerId);
    Optional<SessionTrackerPersistenceEntity> findByReservationId(String reservationId);
    Optional<SessionTrackerPersistenceEntity> findFirstByEquipmentIdAndSessionIsActiveTrue(String equipmentId);
    List<SessionTrackerPersistenceEntity> findAllBySessionIsActiveTrue();
    void deleteBySessionTrackerId(String sessionTrackerId);
}
