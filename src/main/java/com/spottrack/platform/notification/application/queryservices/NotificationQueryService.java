package com.spottrack.platform.notification.application.queryservices;

import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;
import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.domain.model.queries.GetActiveEquipmentWatchesQuery;
import com.spottrack.platform.notification.domain.model.queries.GetClientNotificationsQuery;

import java.util.List;

public interface NotificationQueryService {
    List<ClientNotification> handle(GetClientNotificationsQuery query);

    List<EquipmentWatch> handle(GetActiveEquipmentWatchesQuery query);
}
