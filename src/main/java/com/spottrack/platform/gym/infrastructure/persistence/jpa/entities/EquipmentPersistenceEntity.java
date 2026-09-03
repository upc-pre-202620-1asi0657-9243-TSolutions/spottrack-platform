package com.spottrack.platform.gym.infrastructure.persistence.jpa.entities;


import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonAppend;

import java.time.LocalDate;

@Entity
@Table(name="equipments")
@Getter
@Setter
@NoArgsConstructor
public class EquipmentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, unique = true)
    private String equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    @Column(nullable = false)
    private String equipmentName;
    @Column(nullable = false)
    private String model;

    @Embedded
    @AttributeOverride(name = "amount",   column = @Column(name = "purchase_amount",   nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "purchase_currency", nullable = false))
    private Money purchasePrice;

    @Embedded
    @AttributeOverride(name = "uuid", column = @Column(name = "manufacturer_id", nullable = false))
    private ManufacturerId manufacturerId;

    @Column(nullable = true)
    private LocalDate maintenanceThreshold;

    @Column(nullable = false)
    private boolean maintenanceAlertSent;

    @Column(nullable = true)
    private String zoneId;
}
