package com.spottrack.platform.reservation.domain.model.aggregates;

import com.spottrack.platform.reservation.domain.model.commands.CreateReservationFromRequest;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.events.ExpressReservationInitiatedEvent;
import com.spottrack.platform.reservation.domain.model.events.ReservationCancelledEvent;
import com.spottrack.platform.reservation.domain.model.events.ReservationEndedEvent;
import com.spottrack.platform.reservation.domain.model.events.ReservationExpiredEvent;
import com.spottrack.platform.reservation.domain.model.events.ReservationTimerStartedEvent;
import com.spottrack.platform.reservation.domain.model.valueobjects.*;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    // Express holds only: instant after which an un-checked-in reservation expires.
    // Null for reservations created from a ReservationRequest.
    private LocalDateTime holdExpiresAt;

    private TimeInterval timeInterval;

    // Not persisted — only meaningful during the initial save() that follows
    // construction, to decide whether onInitiated() should fire. Reservations
    // reconstructed from persistence default this to false, which is correct:
    // onInitiated() is only ever invoked on the isNew branch of save().
    private boolean expressInitiated;

    private static final LocalTime LATEST_DERIVED_START = LocalTime.of(23, 58);
    private static final LocalTime LATEST_END = LocalTime.of(23, 59);

    protected Reservation() {}

    /**
     * Express reservation: client skips the request flow and holds equipment directly
     * for holdMinutes. Starts in ACTIVE status with the timer not yet running.
     * The usage window (startTime/endTime) is kept as sent; any missing bound is derived
     * and has no influence on the hold.
     */
    public Reservation(InitiateExpressReservation command, LocalDateTime now, int holdMinutes) {
        if (holdMinutes <= 0) {
            throw new IllegalArgumentException("holdMinutes must be positive");
        }
        this.id = new ReservationId(UUID.randomUUID().toString());
        this.clientId = command.clientId();
        this.equipmentId = command.equipmentId();
        this.status = ReservationStatus.ACTIVE;
        this.startedAt = now;
        this.holdExpiresAt = now.plusMinutes(holdMinutes);
        this.timeInterval = resolveTimeInterval(command, now, holdMinutes);
        this.expressInitiated = true;
    }

    private static TimeInterval resolveTimeInterval(InitiateExpressReservation command, LocalDateTime now, int holdMinutes) {
        var start = command.startTime() != null
                ? command.startTime().toLocalTime()
                : minOf(now.toLocalTime().withSecond(0).withNano(0), LATEST_DERIVED_START);
        var end = command.endTime() != null
                ? command.endTime().toLocalTime()
                : LocalTime.ofSecondOfDay(Math.min(start.toSecondOfDay() + holdMinutes * 60L, LATEST_END.toSecondOfDay()));
        return new TimeInterval(Time.valueOf(start), Time.valueOf(end));
    }

    private static LocalTime minOf(LocalTime a, LocalTime b) {
        return a.isBefore(b) ? a : b;
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


    public Reservation(String id, Long clientId, String equipmentId, String status, LocalDateTime startedAt, LocalDateTime timerExpiry, LocalDateTime holdExpiresAt, Time startTime, Time endTime){
        this.id = new ReservationId(id);  // String → ReservationId value object
        this.clientId = new ClientId(clientId);
        this.equipmentId = new EquipmentId(equipmentId);
        this.status = ReservationStatus.valueOf(status);  // String → Enum
        this.timerExpiry = timerExpiry;
        this.holdExpiresAt = holdExpiresAt;
        this.startedAt = startedAt;
        this.timeInterval = new TimeInterval(startTime, endTime); // construct the whole object
    }
    /**
     * True while this is an express hold the client has not checked in to and whose
     * hold window has elapsed at the given instant.
     */
    public boolean isHoldExpiredAt(LocalDateTime now) {
        return this.status == ReservationStatus.ACTIVE
                && this.timerExpiry == null
                && this.holdExpiresAt != null
                && !now.isBefore(this.holdExpiresAt);
    }

    /**
     * Sets the timer expiry (check-in). Only makes sense while the reservation is ACTIVE
     * and, for express holds, before the hold elapsed.
     * durationMinutes comes from the StartReservationTimer command.
     */
    public void startTimer(int durationMinutes, LocalDateTime now) {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("reservation.error.timerOnlyForActive");
        }
        if (isHoldExpiredAt(now)) {
            throw new IllegalStateException("reservation.error.holdExpired");
        }
        this.timerExpiry = now.plusMinutes(durationMinutes);
        registerDomainEvent(new ReservationTimerStartedEvent(this.id.uuid(), this.equipmentId.uuid(), this.timerExpiry));
    }

    /**
     * The hold elapsed without a check-in: the reservation expires and the equipment
     * should be released.
     */
    public void expire(LocalDateTime now) {
        if (!isHoldExpiredAt(now)) {
            throw new IllegalStateException("reservation.error.expireOnlyForElapsedHold");
        }
        this.status = ReservationStatus.EXPIRED;
        registerDomainEvent(new ReservationExpiredEvent(this.id.uuid(), this.equipmentId.uuid(), this.clientId.clientId()));
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
