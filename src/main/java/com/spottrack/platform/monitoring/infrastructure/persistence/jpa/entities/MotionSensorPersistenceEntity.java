package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters.EquipmentIdPersistenceConverter;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters.MotionSensorIdPersistenceConverter;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "motion_sensors")
@Getter
@Setter
public class MotionSensorPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = MotionSensorIdPersistenceConverter.class)
    @Column(nullable = false, unique = true)
    private MotionSensorId motionSensorId;

    @Convert(converter = EquipmentIdPersistenceConverter.class)
    @Column(nullable = false, unique = true)
    private EquipmentId equipmentId;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    private boolean online;

    @Column(nullable = false)
    private LocalDateTime lastStatusChangeAt;
}
