package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.commands.CreateReservationFromRequest;
import com.spottrack.platform.reservation.domain.model.events.RequestOccupyEquipmentSubmittedEvent;
import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Policy: [Request Occupy Equipment submitted] → create the Reservation that
 * backs it, so StartReservationTimer has something to operate on (the "normal"
 * non-express path from the event-storming board). ReservationCommandServiceImpl
 * guards against the Express flow's reverse side effect double-firing this.
 */
@Service
public class ReservationAutoCreationOnRequestSubmittedEventHandler {

    private final ReservationCommandService reservationCommandService;

    public ReservationAutoCreationOnRequestSubmittedEventHandler(ReservationCommandService reservationCommandService) {
        this.reservationCommandService = reservationCommandService;
    }

    @EventListener
    public void on(RequestOccupyEquipmentSubmittedEvent event) {
        reservationCommandService.handle(new CreateReservationFromRequest(
                new ClientId(event.clientId()), new EquipmentId(event.equipmentId())));
    }
}
