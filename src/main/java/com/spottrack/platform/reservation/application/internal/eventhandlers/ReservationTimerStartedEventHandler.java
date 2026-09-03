package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.events.ReservationTimerStartedEvent;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * Policy: [Reservation Timer Status = Expired] → Request Status Change to Available
 *
 * When the timer starts, this handler schedules a task to fire at the exact timerExpiry instant.
 * When that task fires, it ends the reservation — which in turn publishes ReservationEndedEvent,
 * triggering the equipment release and alerts notification downstream.
 *
 * The scheduled task runs on a separate thread (reservationTaskScheduler pool),
 * outside the original HTTP request transaction.
 */
@Component
public class ReservationTimerStartedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ReservationTimerStartedEventHandler.class);

    private final TaskScheduler taskScheduler;
    private final ReservationCommandService reservationCommandService;

    public ReservationTimerStartedEventHandler(TaskScheduler taskScheduler,
                                               ReservationCommandService reservationCommandService) {
        this.taskScheduler = taskScheduler;
        this.reservationCommandService = reservationCommandService;
    }

    @EventListener
    public void on(ReservationTimerStartedEvent event) {
        var expiryInstant = event.timerExpiry().atZone(ZoneId.systemDefault()).toInstant();

        // Schedule the expiry task to run exactly when the timer runs out
        taskScheduler.schedule(() -> onTimerExpired(event.reservationId()), expiryInstant);

        log.info("Timer scheduled for reservation {} — expires at {}", event.reservationId(), event.timerExpiry());
    }

    private void onTimerExpired(String reservationId) {
        log.info("Timer expired for reservation {} — ending reservation", reservationId);
        var command = new EndReservation(new ReservationId(reservationId));
        reservationCommandService.handle(command);
    }
}
