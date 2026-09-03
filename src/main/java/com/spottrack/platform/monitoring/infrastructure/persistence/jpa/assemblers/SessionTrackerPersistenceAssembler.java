package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.SessionTrackerPersistenceEntity;

import java.time.ZoneId;

public class SessionTrackerPersistenceAssembler {
    public static SessionTracker toDomainFromPersistence(SessionTrackerPersistenceEntity entity){
        return new SessionTracker(
                entity.getId(), entity.getSessionTrackerId(), entity.getEquipmentId(), entity.getReservationId(),
                entity.getContinuousActivity(), entity.getSeconds(), entity.isSessionIsActive(), entity.isSessionIsInactive(),
                entity.getLastActivityAt(),
                entity.getCreatedAt() != null
                        ? entity.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null);
    }

    public static SessionTrackerPersistenceEntity toPersistenceFromDomain(SessionTracker entity){
        var persistenceEntity = new SessionTrackerPersistenceEntity();
        persistenceEntity.setId(entity.getId());
        persistenceEntity.setSessionTrackerId(entity.getSessionTrackerId().uuid());
        persistenceEntity.setEquipmentId(entity.getEquipmentId().uuid());
        persistenceEntity.setReservationId(entity.getReservationId() != null ? entity.getReservationId().uuid() : null);
        persistenceEntity.setContinuousActivity(entity.getUsageActivity().continuousActivity());
        persistenceEntity.setSeconds((entity.getUsageActivity().seconds()));
        persistenceEntity.setSessionIsActive(entity.isSessionIsActive());
        persistenceEntity.setSessionIsInactive(entity.isSessionIsInactive());
        persistenceEntity.setLastActivityAt(entity.getLastActivityAt());
        return persistenceEntity;
    }
}
