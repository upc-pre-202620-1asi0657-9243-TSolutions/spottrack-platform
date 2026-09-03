package com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.converters.MaintenanceQuoteIdPersistenceConverter;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maintenance_quotes")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceQuotePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = MaintenanceQuoteIdPersistenceConverter.class)
    @Column(nullable = false, unique = true)
    private MaintenanceQuoteId maintenanceQuoteId;

    @Column(nullable = true)
    private String equipmentId;

    @Column(nullable = false)
    private Double correctiveActionsCost;

    @Column(nullable = false)
    private Double sparePartsCost;

    @Column(nullable = false)
    private Double preventiveCost;

    @Column(nullable = false)
    private Double totalMaintenanceCost;
}
