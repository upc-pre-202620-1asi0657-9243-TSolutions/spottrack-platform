package com.spottrack.platform.notification.domain.model.aggregates;

import com.spottrack.platform.notification.domain.model.valueobjects.NotificationType;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

/**
 * An in-app notification addressed to a client about one equipment.
 */
@Getter
public class ClientNotification extends AbstractDomainAggregateRoot<ClientNotification> {

    @Setter
    private Long id;

    private final Long clientId;

    private final String equipmentId;

    private final NotificationType type;

    private final String message;

    /** Null while unread. */
    private Date readAt;

    private final Date createdAt;

    public ClientNotification(Long id, Long clientId, String equipmentId, NotificationType type,
                              String message, Date readAt, Date createdAt) {
        this.id = id;
        this.clientId = Objects.requireNonNull(clientId, "Client id must not be null");
        this.equipmentId = Objects.requireNonNull(equipmentId, "Equipment id must not be null");
        this.type = Objects.requireNonNull(type, "Type must not be null");
        this.message = Objects.requireNonNull(message, "Message must not be null");
        this.readAt = readAt;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
    }

    public static ClientNotification create(Long clientId, String equipmentId, NotificationType type,
                                            String message, Date now) {
        return new ClientNotification(null, clientId, equipmentId, type, message, null, now);
    }

    public boolean isRead() {
        return readAt != null;
    }

    /** Idempotent: marking an already read notification keeps the original read time. */
    public void markAsRead(Date now) {
        if (readAt != null) {
            return;
        }
        this.readAt = Objects.requireNonNull(now, "Read time must not be null");
    }
}
