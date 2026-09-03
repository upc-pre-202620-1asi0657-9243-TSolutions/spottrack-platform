package com.spottrack.platform.reservation.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.entities.ReservationRequestPersistenceEntity;

public class ReservationRequestPersistenceAssembler {

    public static ReservationRequest toDomainFromPersistence(ReservationRequestPersistenceEntity entity) {
        var request = new ReservationRequest(
                entity.getUuid(),
                entity.getClientId(),
                entity.getEquipmentId(),
                entity.getStatus(),
                entity.getRequestedAt()
        );
        request.setPersistenceId(entity.getId());
        return request;
    }

    public static ReservationRequestPersistenceEntity toPersistenceFromDomain(ReservationRequest domain) {
        var persistence = new ReservationRequestPersistenceEntity();
        persistence.setId(domain.getPersistenceId());
        persistence.setUuid(domain.getId().uuid());
        persistence.setClientId(domain.getClientId().clientId());
        persistence.setEquipmentId(domain.getEquipmentId().uuid());
        persistence.setStatus(domain.getStatus());
        persistence.setRequestedAt(domain.getRequestedAt());
        return persistence;
    }
}
