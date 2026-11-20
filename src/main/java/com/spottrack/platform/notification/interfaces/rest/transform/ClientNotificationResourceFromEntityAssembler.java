package com.spottrack.platform.notification.interfaces.rest.transform;

import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;
import com.spottrack.platform.notification.interfaces.rest.resources.ClientNotificationResource;

public final class ClientNotificationResourceFromEntityAssembler {

    private ClientNotificationResourceFromEntityAssembler() {}

    public static ClientNotificationResource toResourceFromEntity(ClientNotification notification) {
        return new ClientNotificationResource(
                notification.getId(),
                notification.getEquipmentId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
