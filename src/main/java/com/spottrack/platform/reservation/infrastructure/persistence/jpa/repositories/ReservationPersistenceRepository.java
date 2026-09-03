package com.spottrack.platform.reservation.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationPersistenceRepository extends JpaRepository<ReservationPersistenceEntity, Long> {
    Optional<ReservationPersistenceEntity> findByUuid(String uuid);
    List<ReservationPersistenceEntity> findAll();
    Optional<ReservationPersistenceEntity> findById(Long id);
    List<ReservationPersistenceEntity> findByClientId(Long clientId);
    boolean existsByClientIdAndStatus(Long clientId, ReservationStatus status);
    boolean existsByEquipmentIdAndStatus(String equipmentId, ReservationStatus status);
    Optional<ReservationPersistenceEntity> findByEquipmentIdAndStatus(String equipmentId, ReservationStatus status);
    List<ReservationPersistenceEntity> findAllByStatusAndTimerExpiryIsNotNullAndTimerExpiryBefore(ReservationStatus status, LocalDateTime now);
    List<ReservationPersistenceEntity> findAllByStatusAndTimerExpiryIsNullAndStartedAtBefore(ReservationStatus status, LocalDateTime cutoff);
}
