package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.domain.model.events.ReservationExpiredEvent;
import com.spottrack.platform.reservation.interfaces.events.ReservationExpiredIntegrationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReservationExpiredEventHandlerTest {

    @Test
    void republishesTheDomainEventAsAnIntegrationEvent() {
        var publisher = mock(ApplicationEventPublisher.class);
        var handler = new ReservationExpiredEventHandler(publisher);

        handler.on(new ReservationExpiredEvent("r-1", "eq-1", 5L));

        verify(publisher).publishEvent(new ReservationExpiredIntegrationEvent("r-1", "eq-1", 5L));
    }
}
