package com.spottrack.platform.notification.application.commandservices;

import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;
import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.domain.model.commands.MarkNotificationAsReadCommand;
import com.spottrack.platform.notification.domain.model.commands.NotifyAvailabilityChangeCommand;
import com.spottrack.platform.notification.domain.model.commands.RegisterAffectedClientCommand;
import com.spottrack.platform.notification.domain.model.commands.UnwatchEquipmentCommand;
import com.spottrack.platform.notification.domain.model.commands.WatchEquipmentCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

import java.util.List;

public interface NotificationCommandService {
    Result<EquipmentWatch, ApplicationError> handle(WatchEquipmentCommand command);

    Result<EquipmentWatch, ApplicationError> handle(UnwatchEquipmentCommand command);

    Result<EquipmentWatch, ApplicationError> handle(RegisterAffectedClientCommand command);

    /** Returns the notifications created for the clients that follow the equipment. */
    List<ClientNotification> handle(NotifyAvailabilityChangeCommand command);

    Result<ClientNotification, ApplicationError> handle(MarkNotificationAsReadCommand command);
}
