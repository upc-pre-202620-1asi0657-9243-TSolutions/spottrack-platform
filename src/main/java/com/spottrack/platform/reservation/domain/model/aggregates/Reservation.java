package com.spottrack.platform.reservation.domain.model.aggregates;

import com.spottrack.platform.reservation.domain.model.commands.CreateReservationFromRequest;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.events.ExpressReservationInitiatedEvent;
import com.spottrack.platform.reservation.domain.model.events.ReservationCancelledEvent;
import com.spottrack.platform.reservation.domain.model.events.ReservationEndedEvent;
import com.spottrack.platform.reservation.domain.model.events.ReservationTimerStartedEvent;
import com.spottrack.platform.reservation.domain.model.valueobjects.*;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregate root representing an active equipment reservation.
 * Can be created via express flow (no prior request) or from a submitted ReservationRequest.
 *
 * The timer tracks how long the client has to use the equipment.
 * When the timer expires, a policy triggers RequestEquipmentStatusChangeToAvailable.
 */
@Getter
@Setter
public class Reservation extends AbstractDomainAggregateRoot<Reservation> {

    private Long persistenceId;
    private ReservationId id;

    private ClientId clientId;

    // String reference to Equipment bounded context
    private EquipmentId equipmentId;

    private ReservationStatus status;

    private LocalDateTime startedAt;

    // Set when StartReservationTimer is handled — null until the timer begins
    private LocalDateTime timerExpiry;

    private TimeInterval timeInterval;

    // Not persisted — only meaningful during the initial save() that follows
    // construction, to decide whether onInitiated() should fire. Reservations
    // reconstructed from persistence default this to false, which is correct:
    // onInitiated() is only ever invoked on the isNew branch of save().
    private boolean expressInitiated;

    protected Reservation() {}

    /**
     * Express reservation: client skips the request flow and reserves equipment directly.
     * Starts in ACTIVE status with the timer not yet running.
     */
    public Reservation(InitiateExpressReservation command) {
        this.id = new ReservationId(UUID.randomUUID().toString());
        this.clientId = command.clientId();
        this.equipmentId = command.equipmentId();
        this.status = ReservationStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.timeInterval = command.timeInterval();
        this.expressInitiated = true;
    }

    /**
     * Called by the repository's save() for every brand-new Reservation, regardless
     * of which constructor built it — expressInitiated guards this so only the
     * express path raises ExpressReservationInitiatedEvent. Reservations created via
     * CreateReservationFromRequest raise nothing here (see that constructor's note).
     */
    public void onInitiated() {
        if (this.expressInitiated) {
            registerDomainEvent(new ExpressReservationInitiatedEvent(this.id.uuid(), this.equipmentId.uuid(), this.clientId.clientId()));
        }
    }

    /**
     * Created by the policy reacting to RequestOccupyEquipmentSubmittedEvent —
     * the "normal" (non-express) path from the event-storming board. No time
     * window is collected at request time, so it gets a full-day placeholder;
     * the real usage window is governed by the timer (StartReservationTimer),
     * not this interval. No domain event here — the board shows the next
     * client-facing step as Start Reservation Timer, not a creation event.
     */
    public Reservation(CreateReservationFromRequest command) {
        this.id = new ReservationId(UUID.randomUUID().toString());
        this.clientId = command.clientId();
        this.equipmentId = command.equipmentId();
        this.status = ReservationStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.timeInterval = new TimeInterval(Time.valueOf("00:00:00"), Time.valueOf("23:59:00"));
    }


    public Reservation(String id, Long clientId, String equipmentId, String status, LocalDateTime startedAt, LocalDateTime timerExpiry, Time startTime, Time endTime){
        this.id = new ReservationId(id);  // String → ReservationId value object
        this.clientId = new ClientId(clientId);
        this.equipmentId = new EquipmentId(equipmentId);
        this.status = ReservationStatus.valueOf(status);  // String → Enum
        this.timerExpiry = timerExpiry;
        this.startedAt = startedAt;
        this.timeInterval = new TimeInterval(startTime, endTime); // construct the whole object
    }
    /**
     *  Sets the timer expiry. Only makes sense when the reservation is ACTIVE.
     * durationMinutes comes from the StartReservationTimer command.
     */
    public void startTimer(int durationMinutes) {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("reservation.error.timerOnlyForActive");
        }
        this.timerExpiry = LocalDateTime.now().plusMinutes(durationMinutes);
        registerDomainEvent(new ReservationTimerStartedEvent(this.id.uuid(), this.equipmentId.uuid(), this.timerExpiry));
    }

    /**
     * Client cancels the reservation. Equipment should be released after this.
     */
    public void cancel() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("reservation.error.cancelOnlyForActive");
        }
        this.status = ReservationStatus.CANCELLED;
        registerDomainEvent(new ReservationCancelledEvent(this.id.uuid(), this.equipmentId.uuid(), this.clientId.clientId()));
    }

    /**
     * Client ends the reservation early. Triggers a notification to the Alerts context.
     */
    public void end() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("reservation.error.endOnlyForActive");
        }
        this.status = ReservationStatus.ENDED;
        registerDomainEvent(new ReservationEndedEvent(this.id.uuid(), this.equipmentId.uuid(), this.clientId.clientId()));
    }
}
