package com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usage_sessions", indexes = {
        @Index(name = "idx_usage_sessions_equipment_started", columnList = "equipmentId, startedAt")
})
@Getter
@Setter
@NoArgsConstructor
public class UsageSessionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, unique = true)
    private String usageSessionId;

    @Column
    private String sessionTrackerId;

    @Column(nullable = false)
    private String equipmentId;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;
}
