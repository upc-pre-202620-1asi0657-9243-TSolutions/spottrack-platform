package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.commands.ExpireReservation;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.repositories.ReservationPersistenceRepository;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Backstop for ReservationTimerStartedEventHandler's precise TaskScheduler-based
 * expiry: that task lives in memory only, so a restart between timer-start and
 * expiry would otherwise leave the reservation ACTIVE forever. This polls for
 * anything still ACTIVE past its timerExpiry and ends it the same way the
 * primary path does — the ACTIVE filter means it never re-matches a reservation
 * the primary path already ended, so there's no double-handling.
 *
 * It is also the hybrid expiry backstop for express holds: the lazy check in the
 * reservation flow expires a stale hold on demand, this poller catches the rest.
 */
@Service
public class ReservationTimerExpiryScheduler {
    private final ReservationPersistenceRepository reservationPersistenceRepository;
    private final ReservationCommandService reservationCommandService;
    private final Clock clock;

    public ReservationTimerExpiryScheduler(ReservationPersistenceRepository reservationPersistenceRepository,
                                           ReservationCommandService reservationCommandService,
                                           Clock clock) {
        this.reservationPersistenceRepository = reservationPersistenceRepository;
        this.reservationCommandService = reservationCommandService;
        this.clock = clock;
    }

    // Reservations without a hold window (created from a ReservationRequest) must check
    // in (start the timer via QR) within this window after reserving. Otherwise the
    // never-activated reservation would stay ACTIVE forever and block the client
    // from reserving anything else.
    private static final long ACTIVATION_WINDOW_MINUTES = 5;

    @Scheduled(fixedRateString = "${spottrack.reservation.expiry-poll-millis:15000}")
    public void checkReservationStatus(){
        var now = LocalDateTime.now(clock);
        List<ReservationPersistenceEntity> expiredReservations = reservationPersistenceRepository.findAllByStatusAndTimerExpiryIsNotNullAndTimerExpiryBefore(ReservationStatus.ACTIVE, now);

        expiredReservations.forEach(reservation ->
                reservationCommandService.handle(new EndReservation(new ReservationId(reservation.getUuid()))));

        // Express holds never checked in past their hold window: expire them so the
        // equipment is released and the client can reserve again.
        List<ReservationPersistenceEntity> elapsedHolds = reservationPersistenceRepository.findAllByStatusAndTimerExpiryIsNullAndHoldExpiresAtIsNotNullAndHoldExpiresAtBefore(ReservationStatus.ACTIVE, now);

        elapsedHolds.forEach(reservation ->
                reservationCommandService.handle(new ExpireReservation(new ReservationId(reservation.getUuid()))));

        // Reservations without a hold window never checked in past their activation window:
        // end them the same way so they stop blocking new reservations.
        LocalDateTime activationCutoff = now.minusMinutes(ACTIVATION_WINDOW_MINUTES);
        List<ReservationPersistenceEntity> unactivatedReservations = reservationPersistenceRepository.findAllByStatusAndTimerExpiryIsNullAndHoldExpiresAtIsNullAndStartedAtBefore(ReservationStatus.ACTIVE, activationCutoff);

        unactivatedReservations.forEach(reservation ->
                reservationCommandService.handle(new EndReservation(new ReservationId(reservation.getUuid()))));
    }

}
