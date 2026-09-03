package com.spottrack.platform.monitoring.domain.repositories;

import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;

import java.util.List;
import java.util.Optional;

public interface SessionTrackerRepository {
    Optional<SessionTracker> findSessionByUuid(SessionTrackerId uuid);
    Optional<SessionTracker> findByReservationId(ReservationId reservationId);
    Optional<SessionTracker> findActiveByEquipmentId(EquipmentId equipmentId);
    List<SessionTracker> findAllBySessionIsActive(SessionTrackerId uuid, boolean active);
    List<SessionTracker> findAll();
    SessionTracker save(SessionTracker sessionTracker);
    void deleteBySessionTrackerId(SessionTrackerId sessionTrackerId);
}
