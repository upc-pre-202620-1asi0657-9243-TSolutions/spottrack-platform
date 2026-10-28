package com.spottrack.platform.shared.domain.model.aggregates;

import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

public class Alert extends AbstractDomainAggregateRoot<Alert> {

    @Getter
    @Setter
    private Long id;

    @Getter
    private Long adminUserId;

    @Getter
    private String equipmentId;

    @Getter
    private AlertSeverity severity;

    /** Null for alerts created before the source was tracked. */
    @Getter
    private AlertSource source;

    @Getter
    private String message;

    @Getter
    private boolean resolved;

    /** Null while pending, and for alerts resolved before the timestamp was tracked. */
    @Getter
    private Date resolvedAt;

    /** Null while the alert is still shown in the alert center. */
    @Getter
    private Date clearedAt;

    @Getter
    private Date createdAt;

    public Alert(Long id, Long adminUserId, String equipmentId, AlertSeverity severity, AlertSource source,
                 String message, boolean resolved, Date resolvedAt, Date clearedAt, Date createdAt) {
        this.id = id;
        this.adminUserId = Objects.requireNonNull(adminUserId, "Admin user id must not be null");
        this.equipmentId = Objects.requireNonNull(equipmentId, "Equipment id must not be null");
        this.severity = Objects.requireNonNull(severity, "Severity must not be null");
        this.source = source;
        this.message = Objects.requireNonNull(message, "Message must not be null");
        this.resolved = resolved;
        this.resolvedAt = resolvedAt;
        this.clearedAt = clearedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
    }

    public boolean isCleared() {
        return clearedAt != null;
    }

    /** Idempotent: resolving an already resolved alert keeps the original resolution time. */
    public void resolve(Date now) {
        if (resolved) {
            return;
        }
        this.resolved = true;
        this.resolvedAt = Objects.requireNonNull(now, "Resolution time must not be null");
    }

    /** Idempotent: clearing an already cleared alert keeps the original clearing time. */
    public void clear(Date now) {
        if (!resolved) {
            throw new IllegalStateException("Only resolved alerts can be cleared; resolve the alert first.");
        }
        if (clearedAt != null) {
            return;
        }
        this.clearedAt = Objects.requireNonNull(now, "Clearing time must not be null");
    }
}
