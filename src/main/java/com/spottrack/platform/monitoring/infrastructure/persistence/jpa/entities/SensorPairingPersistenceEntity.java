package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters.MotionSensorIdPersistenceConverter;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_pairings", indexes = @Index(name = "idx_sensor_pairings_sensor", columnList = "motion_sensor_id"))
@Getter
@Setter
public class SensorPairingPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = MotionSensorIdPersistenceConverter.class)
    @Column(name = "motion_sensor_id", nullable = false)
    private MotionSensorId motionSensorId;

    @Column(nullable = false, length = 100)
    private String secretHash;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SensorPairingStatus status;

    private LocalDateTime usedAt;
}
