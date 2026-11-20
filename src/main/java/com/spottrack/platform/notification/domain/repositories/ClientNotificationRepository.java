package com.spottrack.platform.notification.domain.repositories;

import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;

import java.util.List;
import java.util.Optional;

public interface ClientNotificationRepository {
    Optional<ClientNotification> findById(Long id);

    /** Notifications of the client, newest first; {@code unreadOnly} leaves out the ones already read. */
    List<ClientNotification> findAllByClientId(Long clientId, boolean unreadOnly);

    ClientNotification save(ClientNotification notification);
}
