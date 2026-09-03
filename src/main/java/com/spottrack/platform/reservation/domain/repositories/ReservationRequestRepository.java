package com.spottrack.platform.reservation.domain.repositories;

import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;

import java.util.Optional;

public interface ReservationRequestRepository {
    Optional<ReservationRequest> findById(Long id);
    Optional<ReservationRequest> findByUuid(String uuid);
    ReservationRequest save(ReservationRequest request);
}
