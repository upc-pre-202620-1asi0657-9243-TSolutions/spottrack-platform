package com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.converters.ActivityReportIdPersistenceConverter;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_reports")
@Getter
@Setter
@NoArgsConstructor
public class ActivityReportPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = ActivityReportIdPersistenceConverter.class)
    @Column(nullable = false, unique = true)
    private ActivityReportId activityReportId;

    @Column(nullable = true)
    private String equipmentId;

    @Column(nullable = false)
    private Long totalUsageTime;

    @Column(nullable = false)
    private Long downtimeCost;

    @Column(nullable = false)
    private Double percentageComparison;
}
