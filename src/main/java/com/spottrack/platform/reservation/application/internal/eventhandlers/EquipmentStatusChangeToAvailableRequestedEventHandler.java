package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.events.EquipmentStatusChangeToAvailableRequestedEvent;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.repositories.ReservationPersistenceRepository;
import com.spottrack.platform.reservation.interfaces.events.EquipmentStatusChangeToAvailableRequestedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service("reservationEquipmentStatusChangeToAvailableRequestedEventHandler")
public class EquipmentStatusChangeToAvailableRequestedEventHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final ReservationCommandService reservationCommandService;
    private final ReservationPersistenceRepository reservationPersistenceRepository;

    public EquipmentStatusChangeToAvailableRequestedEventHandler(
            ApplicationEventPublisher eventPublisher,
            ReservationCommandService reservationCommandService,
            ReservationPersistenceRepository reservationPersistenceRepository) {
        this.eventPublisher = eventPublisher;
        this.reservationCommandService = reservationCommandService;
        this.reservationPersistenceRepository = reservationPersistenceRepository;
    }

    @EventListener
    public void on(EquipmentStatusChangeToAvailableRequestedEvent event) {
        eventPublisher.publishEvent(new EquipmentStatusChangeToAvailableRequestedIntegrationEvent(
                event.requestId(),
                event.equipmentId(),
                "AVAILABLE"));

        reservationPersistenceRepository
                .findByEquipmentIdAndStatus(event.equipmentId(), ReservationStatus.ACTIVE)
                .ifPresent(entity -> reservationCommandService.handle(
                        new EndReservation(new ReservationId(entity.getUuid()))));
    }
}
