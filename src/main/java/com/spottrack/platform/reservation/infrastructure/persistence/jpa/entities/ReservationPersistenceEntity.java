package com.spottrack.platform.reservation.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.time.LocalDateTime;


@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
public class ReservationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, unique = true)
    private String uuid;

    // Reference to Client bounded context — no JPA join, just the UUID
    @Column(nullable = false)
    Long clientId;

    // Reference to Equipment bounded context — no JPA join, just the UUID
    @Column(nullable = false)
    private String equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = true)
    private LocalDateTime timerExpiry;

    @Column(nullable = true)
    private LocalDateTime holdExpiresAt;

    // Concurrency guards: only populated while the reservation is ACTIVE (client key:
    // only for express holds), so MySQL's unique index (which allows repeated NULLs)
    // rejects a second concurrent active reservation for the same equipment/client.
    @Column(nullable = true, unique = true)
    private String activeEquipmentKey;

    @Column(nullable = true, unique = true)
    private Long activeClientKey;

    @Column(nullable = false)
    private Time startTime;

    @Column(nullable = false)
    private Time endTime;
}
