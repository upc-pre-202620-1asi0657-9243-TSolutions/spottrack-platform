package com.spottrack.platform.reservation.domain.repositories;

import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.shared.application.result.ApplicationError;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Optional<Reservation> findByUuid(String Id);
    Optional<Reservation> findById(Long Id);
    List<Reservation> findAll();
    List<Reservation> findByClientId(Long clientId);
    boolean existsByClientIdAndStatus(Long clientId, ReservationStatus status);
    boolean existsByEquipmentIdAndStatus(String equipmentId, ReservationStatus status);
    Reservation save(Reservation reservation);
    Optional<Reservation> findByStatus(ReservationStatus status);
}

