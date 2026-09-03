package com.spottrack.platform.shared.domain.model.aggregates;

import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
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

    @Getter
    private String message;

    @Getter
    private boolean resolved;

    @Getter
    private Date createdAt;

    public Alert(Long id, Long adminUserId, String equipmentId, AlertSeverity severity, String message,
                 boolean resolved, Date createdAt) {
        this.id = id;
        this.adminUserId = Objects.requireNonNull(adminUserId, "Admin user id must not be null");
        this.equipmentId = Objects.requireNonNull(equipmentId, "Equipment id must not be null");
        this.severity = Objects.requireNonNull(severity, "Severity must not be null");
        this.message = Objects.requireNonNull(message, "Message must not be null");
        this.resolved = resolved;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
    }

    public void resolve() {
        this.resolved = true;
    }
}
