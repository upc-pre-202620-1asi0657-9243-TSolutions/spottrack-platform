package com.spottrack.platform.notification.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.notification.domain.model.valueobjects.WatchOrigin;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchStatus;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "equipment_watches", indexes = {
        @Index(name = "idx_equipment_watches_equipment_status", columnList = "equipment_id, status"),
        @Index(name = "idx_equipment_watches_client_status", columnList = "client_id, status")
})
public class EquipmentWatchPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "equipment_id", nullable = false)
    private String equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false)
    private WatchOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WatchStatus status;

    @Column(name = "notified_at")
    private Date notifiedAt;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }

    public WatchOrigin getOrigin() { return origin; }
    public void setOrigin(WatchOrigin origin) { this.origin = origin; }

    public WatchStatus getStatus() { return status; }
    public void setStatus(WatchStatus status) { this.status = status; }

    public Date getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(Date notifiedAt) { this.notifiedAt = notifiedAt; }
}
