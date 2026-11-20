package com.spottrack.platform.notification.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;
import com.spottrack.platform.notification.infrastructure.persistence.jpa.entities.ClientNotificationPersistenceEntity;

public final class ClientNotificationPersistenceAssembler {

    private ClientNotificationPersistenceAssembler() {}

    public static ClientNotification toDomainFromPersistence(ClientNotificationPersistenceEntity entity) {
        return new ClientNotification(
                entity.getId(),
                entity.getClientId(),
                entity.getEquipmentId(),
                entity.getType(),
                entity.getMessage(),
                entity.getReadAt(),
                entity.getCreatedAt()
        );
    }

    public static ClientNotificationPersistenceEntity toPersistenceFromDomain(ClientNotification notification) {
        var entity = new ClientNotificationPersistenceEntity();
        entity.setId(notification.getId());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setClientId(notification.getClientId());
        entity.setEquipmentId(notification.getEquipmentId());
        entity.setType(notification.getType());
        entity.setMessage(notification.getMessage());
        entity.setReadAt(notification.getReadAt());
        return entity;
    }
}
