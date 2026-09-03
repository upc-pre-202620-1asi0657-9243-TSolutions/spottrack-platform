package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.application.commandServices.ReservationRequestCommandService;
import com.spottrack.platform.reservation.domain.model.commands.SubmitRequestOccupyEquipment;
import com.spottrack.platform.reservation.domain.model.events.ExpressReservationInitiatedEvent;
import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ExpressReservationInitiatedEventHandler {
    private final ReservationRequestCommandService reservationRequestCommandService;

    public ExpressReservationInitiatedEventHandler(ReservationRequestCommandService reservationRequestcommandService){
        this.reservationRequestCommandService = reservationRequestcommandService;
    }

    @EventListener
    public void on(ExpressReservationInitiatedEvent event) {
        var command = new SubmitRequestOccupyEquipment(new ClientId(event.clientId()), new EquipmentId(event.equipmentId()));
        reservationRequestCommandService.handle(command);
    }
}
