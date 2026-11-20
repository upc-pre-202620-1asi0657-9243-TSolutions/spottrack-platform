package com.spottrack.platform.notification.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.notification.domain.model.valueobjects.NotificationType;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "client_notifications",
        indexes = @Index(name = "idx_client_notifications_client", columnList = "client_id"))
public class ClientNotificationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "equipment_id", nullable = false)
    private String equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "read_at")
    private Date readAt;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getReadAt() { return readAt; }
    public void setReadAt(Date readAt) { this.readAt = readAt; }
}
