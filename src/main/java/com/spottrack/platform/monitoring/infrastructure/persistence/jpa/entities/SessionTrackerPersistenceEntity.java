package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name="session_trackers")
@NoArgsConstructor
@Getter
@Setter
public class SessionTrackerPersistenceEntity extends AuditableAbstractPersistenceEntity {
    /**
     * For security measures, we will use uuids as secondary Ids aside from the real DB Long Ids
     */
    @Column(nullable = false, unique = true)
    String sessionTrackerId;
    /**
     * id of the equipment being used — always present, regardless of whether
     * usage is tied to a reservation.
     */
    @Column(nullable = false)
    String equipmentId;
    /**
     * id of the reservation that the session tracker is monitoring; null for
     * walk-up usage (equipment used without a booked reservation).
     */
    @Column(unique = true)
    String  reservationId;
    @Column(nullable = false)
    boolean sessionIsInactive;
    @Column(nullable = false)
    boolean sessionIsActive;
    @Column(nullable = false)
    LocalTime seconds;
    @Column(nullable = false)
    LocalTime continuousActivity;
    @Column
    LocalDateTime lastActivityAt;

}
