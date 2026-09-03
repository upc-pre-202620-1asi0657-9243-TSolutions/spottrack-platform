package com.spottrack.platform.reservation.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationRequestStatus;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.entities.ReservationRequestPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRequestPersistenceRepository extends JpaRepository<ReservationRequestPersistenceEntity, Long> {
    Optional<ReservationRequestPersistenceEntity> findByUuid(String uuid);
    Optional<ReservationRequestPersistenceEntity> findByEquipmentIdAndStatus(String equipmentId, ReservationRequestStatus status);
}
