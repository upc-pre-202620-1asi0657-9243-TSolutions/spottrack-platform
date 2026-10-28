package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.commands.ExpireReservation;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.repositories.ReservationPersistenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationTimerExpirySchedulerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 27, 18, 30, 0);

    private ReservationPersistenceRepository persistenceRepository;
    private ReservationCommandService commandService;
    private ReservationTimerExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        persistenceRepository = mock(ReservationPersistenceRepository.class);
        commandService = mock(ReservationCommandService.class);
        var clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        scheduler = new ReservationTimerExpiryScheduler(persistenceRepository, commandService, clock);
        when(persistenceRepository.findAllByStatusAndTimerExpiryIsNotNullAndTimerExpiryBefore(any(), any())).thenReturn(List.of());
        when(persistenceRepository.findAllByStatusAndTimerExpiryIsNullAndHoldExpiresAtIsNotNullAndHoldExpiresAtBefore(any(), any())).thenReturn(List.of());
        when(persistenceRepository.findAllByStatusAndTimerExpiryIsNullAndHoldExpiresAtIsNullAndStartedAtBefore(any(), any())).thenReturn(List.of());
    }

    private static ReservationPersistenceEntity entity(String uuid) {
        var entity = new ReservationPersistenceEntity();
        entity.setUuid(uuid);
        return entity;
    }

    @Test
    void elapsedHoldsAreExpired() {
        when(persistenceRepository.findAllByStatusAndTimerExpiryIsNullAndHoldExpiresAtIsNotNullAndHoldExpiresAtBefore(
                ReservationStatus.ACTIVE, NOW)).thenReturn(List.of(entity("r-1")));

        scheduler.checkReservationStatus();

        verify(commandService).handle(new ExpireReservation(new ReservationId("r-1")));
        verify(commandService, never()).handle(new EndReservation(new ReservationId("r-1")));
    }

    @Test
    void reservationsWithoutHoldKeepTheFiveMinuteActivationWindow() {
        when(persistenceRepository.findAllByStatusAndTimerExpiryIsNullAndHoldExpiresAtIsNullAndStartedAtBefore(
                ReservationStatus.ACTIVE, NOW.minusMinutes(5))).thenReturn(List.of(entity("r-2")));

        scheduler.checkReservationStatus();

        verify(commandService).handle(new EndReservation(new ReservationId("r-2")));
    }

    @Test
    void expiredTimersAreStillEnded() {
        when(persistenceRepository.findAllByStatusAndTimerExpiryIsNotNullAndTimerExpiryBefore(ReservationStatus.ACTIVE, NOW))
                .thenReturn(List.of(entity("r-3")));

        scheduler.checkReservationStatus();

        verify(commandService).handle(new EndReservation(new ReservationId("r-3")));
    }
}
