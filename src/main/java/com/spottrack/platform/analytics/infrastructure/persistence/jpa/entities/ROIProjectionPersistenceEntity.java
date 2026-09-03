package com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.converters.ROIProjectionIdPersistenceConverter;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roi_projections")
@Getter
@Setter
@NoArgsConstructor
public class ROIProjectionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = ROIProjectionIdPersistenceConverter.class)
    @Column(nullable = false, unique = true)
    private ROIProjectionId roiProjectionId;

    @Column(nullable = false)
    private Double requestedDowntimeCost;

    @Column(nullable = false)
    private Double requestedEarnings;

    @Column(nullable = false)
    private Double roiIndex;

    @Column(nullable = false)
    private String demandStatus;
}
