package com.spottrack.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.spottrack.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;

import java.util.List;

public class UserPersistenceAssembler {

    public static User toDomainFromPersistence(UserPersistenceEntity entity) {
        List<Role> roles = entity.getRoles().stream()
                .map(RolePersistenceAssembler::toDomainFromPersistence)
                .toList();
        User user = new User(entity.getUsername(), entity.getPassword(), roles);
        user.setId(entity.getId());
        user.setActive(entity.isActive());
        user.setNotifyOnCritical(entity.isNotifyOnCritical());
        user.setNotifyOnWarning(entity.isNotifyOnWarning());
        user.setNotificationEmail(entity.getNotificationEmail());
        return user;
    }

    public static UserPersistenceEntity toPersistenceFromDomain(User user) {
        UserPersistenceEntity entity = new UserPersistenceEntity();
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        List<RolePersistenceEntity> roleEntities = user.getRoles().stream()
                .map(RolePersistenceAssembler::toPersistenceFromDomain)
                .toList();
        entity.setRoles(roleEntities);
        entity.setActive(user.isActive());
        entity.setNotifyOnCritical(user.isNotifyOnCritical());
        entity.setNotifyOnWarning(user.isNotifyOnWarning());
        entity.setNotificationEmail(user.getNotificationEmail());
        return entity;
    }
}
