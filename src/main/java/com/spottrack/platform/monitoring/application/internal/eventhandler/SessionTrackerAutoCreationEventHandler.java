package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.CreateSessionTrackerCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.UsageActivity;
import com.spottrack.platform.reservation.interfaces.events.ExpressReservationInitiatedIntegrationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.UUID;

/**
 * A session tracker's whole reason to exist is to measure usage of a booked
 * equipment, so it's born the moment the reservation is: no manual creation
 * form needed. Activity starts at zero and is filled in as sensors capture
 * real movement (see CameraSensorController/MotionSensorController capture-motion).
 *
 * Creating it eagerly here (rather than waiting for the first sensor reading)
 * is what links the tracker to the reservationId — a sensor reading alone only
 * knows the equipment, not the reservation.
 */
@Service
public class SessionTrackerAutoCreationEventHandler {
    private final SessionTrackerCommandService sessionTrackerCommandService;

    public SessionTrackerAutoCreationEventHandler(SessionTrackerCommandService sessionTrackerCommandService) {
        this.sessionTrackerCommandService = sessionTrackerCommandService;
    }

    @EventListener
    public void on(ExpressReservationInitiatedIntegrationEvent event) {
        var command = new CreateSessionTrackerCommand(
                new SessionTrackerId(UUID.randomUUID().toString()),
                new EquipmentId(event.equipmentId()),
                new ReservationId(event.reservationId()),
                true,
                false,
                new UsageActivity(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT)
        );
        sessionTrackerCommandService.handle(command);
    }
}
