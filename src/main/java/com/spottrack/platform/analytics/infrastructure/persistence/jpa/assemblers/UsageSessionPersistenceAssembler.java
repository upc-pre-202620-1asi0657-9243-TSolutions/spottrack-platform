package com.spottrack.platform.analytics.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.analytics.domain.model.aggregates.UsageSession;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageSessionId;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities.UsageSessionPersistenceEntity;

public final class UsageSessionPersistenceAssembler {

    private UsageSessionPersistenceAssembler() {
    }

    public static UsageSession toDomainFromPersistence(UsageSessionPersistenceEntity entity) {
        if (entity == null) return null;
        return new UsageSession(
                entity.getId(),
                new UsageSessionId(entity.getUsageSessionId()),
                entity.getSessionTrackerId(),
                entity.getEquipmentId(),
                entity.getStartedAt(),
                entity.getEndedAt());
    }

    public static UsageSessionPersistenceEntity toPersistenceFromDomain(UsageSession usageSession) {
        if (usageSession == null) return null;
        var entity = new UsageSessionPersistenceEntity();
        if (usageSession.getId() != null) {
            entity.setId(usageSession.getId());
        }
        entity.setUsageSessionId(usageSession.getUsageSessionId().value());
        entity.setSessionTrackerId(usageSession.getSessionTrackerId());
        entity.setEquipmentId(usageSession.getEquipmentId());
        entity.setStartedAt(usageSession.getStartedAt());
        entity.setEndedAt(usageSession.getEndedAt());
        return entity;
    }
}
