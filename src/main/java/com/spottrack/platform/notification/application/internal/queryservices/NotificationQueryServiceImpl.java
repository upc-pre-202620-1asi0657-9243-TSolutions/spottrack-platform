package com.spottrack.platform.notification.application.internal.queryservices;

import com.spottrack.platform.notification.application.queryservices.NotificationQueryService;
import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;
import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.domain.model.queries.GetActiveEquipmentWatchesQuery;
import com.spottrack.platform.notification.domain.model.queries.GetClientNotificationsQuery;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchStatus;
import com.spottrack.platform.notification.domain.repositories.ClientNotificationRepository;
import com.spottrack.platform.notification.domain.repositories.EquipmentWatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final ClientNotificationRepository notificationRepository;
    private final EquipmentWatchRepository watchRepository;

    public NotificationQueryServiceImpl(ClientNotificationRepository notificationRepository,
                                        EquipmentWatchRepository watchRepository) {
        this.notificationRepository = notificationRepository;
        this.watchRepository = watchRepository;
    }

    @Override
    public List<ClientNotification> handle(GetClientNotificationsQuery query) {
        return notificationRepository.findAllByClientId(query.clientId(), query.unreadOnly());
    }

    /** Only the bells the client turned on; watches registered because of an anomaly report are internal. */
    @Override
    public List<EquipmentWatch> handle(GetActiveEquipmentWatchesQuery query) {
        return watchRepository.findAllByClientIdAndStatus(query.clientId(), WatchStatus.ACTIVE).stream()
                .filter(EquipmentWatch::isRequestedByClient)
                .toList();
    }
}
