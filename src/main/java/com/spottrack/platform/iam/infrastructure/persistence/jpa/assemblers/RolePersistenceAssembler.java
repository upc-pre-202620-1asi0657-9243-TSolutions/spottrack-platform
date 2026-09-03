package com.spottrack.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.model.valueobjects.Roles;
import com.spottrack.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;

public class RolePersistenceAssembler {

    public static Role toDomainFromPersistence(RolePersistenceEntity entity) {
        Role role = new Role();
        role.setId(entity.getId());
        role.setName(Roles.valueOf(entity.getName()));
        return role;
    }

    public static RolePersistenceEntity toPersistenceFromDomain(Role role) {
        RolePersistenceEntity entity = new RolePersistenceEntity();
        if (role.getId() != null) {
            entity.setId(role.getId());
        }
        entity.setName(role.getName().name());
        return entity;
    }
}
