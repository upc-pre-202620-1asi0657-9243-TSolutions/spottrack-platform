package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.monitoring.domain.model.valueobjects.AnomalyId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters.AnomalyIdPersistenceConverter;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters.EquipmentIdPersistenceConverter;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters.ReservationIdPersistenceConverter;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters.ZoneIdPersistenceConverter;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.DiffExclude;

import java.time.LocalDate;

@Entity
@Table(name="anomalies")
@Getter
@Setter
public class AnomalyPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = AnomalyIdPersistenceConverter.class)
    @Column(nullable = false, unique = true)
    private AnomalyId anomalyId;

    @Column(nullable = false)
    @Convert(converter = ReservationIdPersistenceConverter.class)
    private ReservationId reservationId;

    @Convert(converter = EquipmentIdPersistenceConverter.class)
    @Column(nullable = false)
    private EquipmentId equipmentId;

    @Convert(converter = ZoneIdPersistenceConverter.class)
    @Column(nullable = false)
    private ZoneId zoneId;

    @Column(nullable = false)
    private String anomalyDescription;

    @Column(nullable = false)
    private LocalDate emissionDate;

}
