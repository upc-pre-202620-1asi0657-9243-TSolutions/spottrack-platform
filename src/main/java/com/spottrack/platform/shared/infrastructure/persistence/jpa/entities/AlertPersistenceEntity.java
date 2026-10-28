package com.spottrack.platform.shared.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "alerts")
public class AlertPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(name = "equipment_id", nullable = false)
    private String equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity;

    // New columns below are nullable so ddl-auto=update can add them to tables that already hold rows.
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private AlertSource source;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    @Column(name = "resolved_at")
    private Date resolvedAt;

    @Column(name = "cleared_at")
    private Date clearedAt;

    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public AlertSource getSource() { return source; }
    public void setSource(AlertSource source) { this.source = source; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public Date getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Date resolvedAt) { this.resolvedAt = resolvedAt; }

    public Date getClearedAt() { return clearedAt; }
    public void setClearedAt(Date clearedAt) { this.clearedAt = clearedAt; }
}
