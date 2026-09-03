package com.spottrack.platform.monitoring.domain.model.aggregates;

import com.spottrack.platform.monitoring.domain.model.commands.CreateSessionTrackerCommand;
import com.spottrack.platform.monitoring.domain.model.events.CameraMotionCapturedEvent;
import com.spottrack.platform.monitoring.domain.model.events.MotionCapturedEvent;
import com.spottrack.platform.monitoring.domain.model.events.SessionTimeCalculatedEvent;
import com.spottrack.platform.monitoring.domain.model.events.UsageSessionEndedEvent;
import com.spottrack.platform.monitoring.domain.model.events.UsageSessionVerifiedEvent;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.UsageActivity;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class SessionTracker extends AbstractDomainAggregateRoot {
    /**
     * For security measures, we will use uuids as secondary Ids aside from the real DB Long Ids
     */
    Long id;
    SessionTrackerId sessionTrackerId;
    UsageActivity usageActivity;
    /**
     * id of the equipment being used — always present, regardless of whether
     * usage is tied to a reservation.
     */
    EquipmentId equipmentId;
    /**
     * id of the reservation that the session tracker is monitoring; null for
     * walk-up usage (equipment used without a booked reservation).
     */
    ReservationId reservationId;
    boolean sessionIsInactive;
    boolean sessionIsActive;
    LocalDateTime lastActivityAt;
    LocalDateTime createdAt;

    public SessionTracker(CreateSessionTrackerCommand command){
        this.sessionTrackerId = command.sessionTrackerId();
        this.equipmentId = command.equipmentId();
        this.reservationId = command.reservationId();
        this.usageActivity = command.usageActivity();
        this.sessionIsInactive = command.sessionIsInactive();
        this.sessionIsActive = command.sessionIsActive();
        this.createdAt = LocalDateTime.now();
    }


    public SessionTracker(Long id, String sessionTrackerId, String equipmentId, String reservationId, LocalTime continousActivity, LocalTime seconds, boolean sessionIsActive, boolean sessionIsInactive, LocalDateTime lastActivityAt, LocalDateTime createdAt){
        this.id = id;
        this.sessionTrackerId = new SessionTrackerId(sessionTrackerId);
        this.equipmentId = new EquipmentId(equipmentId);
        this.reservationId = reservationId != null ? new ReservationId(reservationId) : null;
        this.usageActivity = new UsageActivity(continousActivity, seconds);
        this.sessionIsActive = sessionIsActive;
        this.sessionIsInactive = sessionIsInactive;
        this.lastActivityAt = lastActivityAt;
        this.createdAt = createdAt;
    }

    /**
     * Records a motion event and refreshes continuousActivity to the elapsed
     * wall-clock time since the session began — that's what actually made it
     * grow past zero. calculateSessionTime() then subtracts whatever gap has
     * built up since the last recorded activity, so a session that goes idle
     * doesn't keep counting time it wasn't really being used.
     */
    public void recordActivity() {
        this.lastActivityAt = LocalDateTime.now();
        if (this.createdAt != null) {
            var elapsedSinceCreation = Duration.between(this.createdAt, this.lastActivityAt);
            this.usageActivity = new UsageActivity(
                    LocalTime.MIDNIGHT.plus(elapsedSinceCreation),
                    this.usageActivity.seconds());
        }
    }

    /**
     * Captured motion keeps the session's activity clock fresh, which is what
     * prevents the inactivity policy from ending the session prematurely.
     */
    public void captureCameraMotion(boolean detected) {
        if (detected) {
            recordActivity();
        }
        registerDomainEvent(new CameraMotionCapturedEvent(this.sessionTrackerId, detected));
    }

    public void captureMotionSensorReading(boolean detected) {
        if (detected) {
            recordActivity();
        }
        registerDomainEvent(new MotionCapturedEvent(this.sessionTrackerId, detected));
    }

    public boolean verifyUsageSession() {
        sessionIsInactive = lastActivityAt != null && Duration.between(lastActivityAt, LocalDateTime.now()).toMinutes() >= 3;
        registerDomainEvent(new UsageSessionVerifiedEvent(this.sessionTrackerId));
        return sessionIsInactive;
    }

    public void endSession(){
        this.sessionIsActive= false;
        this.sessionIsInactive = true;
        registerDomainEvent(new UsageSessionEndedEvent(
                this.sessionTrackerId,
                this.reservationId != null ? this.reservationId.uuid() : null));
    }


    /**
     * This is a simple calculation substracting the inactivity time to the actual continouous activity.
     * A session that never captured any sensor motion has no lastActivityAt to measure
     * inactivity from, so it's reported as zero true activity rather than crashing.
     */
    private LocalTime computeTrueActivity() {
        if (lastActivityAt == null) {
            return LocalTime.MIDNIGHT;
        }
        var activity = this.usageActivity.continuousActivity();
        var inactivity = Duration.between(lastActivityAt, LocalDateTime.now());
        return activity.minus(inactivity);
    }

    /**
     * Read-only preview of the current true activity — safe to call on a still-active
     * session, doesn't mutate state or fire any event. This is what an admin "peek"
     * action should use.
     */
    public LocalTime peekTrueActivity() {
        return computeTrueActivity();
    }

    /**
     * Finalizes the session's activity for reporting: computes the true activity and
     * fires SessionTimeCalculatedEvent, which (via SessionTimeCalculatedEventHandler)
     * reports it to Analytics and deletes this tracker. Only meant to be called once a
     * session has actually ended (see UsageSessionEndedEventHandler) — calling this on
     * a still-active session will delete it prematurely.
     */
    public LocalTime calculateSessionTime() {
        var trueActivity = computeTrueActivity();
        registerDomainEvent(new SessionTimeCalculatedEvent(this.sessionTrackerId, this.equipmentId, trueActivity));
        return trueActivity;
    }

}
