package com.spottrack.platform.reservation.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationRequestId;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationRequestStatus;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="reservation_requests")
@NoArgsConstructor
@Getter
@Setter
public class ReservationRequestPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private String equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ReservationRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

}
