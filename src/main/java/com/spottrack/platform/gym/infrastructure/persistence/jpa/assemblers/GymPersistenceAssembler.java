package com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.GymPersistenceEntity;

public class GymPersistenceAssembler {
    private GymPersistenceAssembler() {}

    public static Gym toDomainFromPersistence(GymPersistenceEntity entity) {
        return new Gym(new GymId(entity.getGymId()), entity.getName(), entity.getAdminUserId());
    }

    public static GymPersistenceEntity toPersistenceFromDomain(Gym entity) {
        var gymPersistenceEntity = new GymPersistenceEntity();
        gymPersistenceEntity.setGymId(entity.getId().uuid());
        gymPersistenceEntity.setName(entity.getName());
        gymPersistenceEntity.setAdminUserId(entity.getAdminUserId());
        return gymPersistenceEntity;
    }
}
