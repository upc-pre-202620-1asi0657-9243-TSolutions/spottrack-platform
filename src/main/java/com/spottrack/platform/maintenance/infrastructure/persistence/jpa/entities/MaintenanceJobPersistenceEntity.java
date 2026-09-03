package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maintenance_jobs")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceJobPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, unique = true)
    private String jobId;

    @Column(nullable = true)
    private String technicianId;

    @Column(nullable = false)
    private String maintenanceId;

    @Column(nullable = false)
    private boolean accepted;
}
