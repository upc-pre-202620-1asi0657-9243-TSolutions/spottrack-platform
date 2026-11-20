package com.spottrack.platform.notification.domain.model.aggregates;

import com.spottrack.platform.notification.domain.model.valueobjects.WatchOrigin;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchStatus;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;

/**
 * A client following an equipment so they are notified when it becomes available again.
 */
@Getter
public class EquipmentWatch extends AbstractDomainAggregateRoot<EquipmentWatch> {

    @Setter
    private Long id;

    private final Long clientId;

    private final String equipmentId;

    private final WatchOrigin origin;

    private WatchStatus status;

    /** Null until the client is told the equipment is available. */
    private Date notifiedAt;

    private final Date createdAt;

    public EquipmentWatch(Long id, Long clientId, String equipmentId, WatchOrigin origin,
                          WatchStatus status, Date notifiedAt, Date createdAt) {
        this.id = id;
        this.clientId = Objects.requireNonNull(clientId, "Client id must not be null");
        this.equipmentId = Objects.requireNonNull(equipmentId, "Equipment id must not be null");
        this.origin = Objects.requireNonNull(origin, "Origin must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.notifiedAt = notifiedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
    }

    public static EquipmentWatch start(Long clientId, String equipmentId, WatchOrigin origin, Date now) {
        return new EquipmentWatch(null, clientId, equipmentId, origin, WatchStatus.ACTIVE, null, now);
    }

    public boolean isActive() {
        return status == WatchStatus.ACTIVE;
    }

    public boolean isNotified() {
        return status == WatchStatus.NOTIFIED;
    }

    public boolean isRequestedByClient() {
        return origin == WatchOrigin.CLIENT_REQUEST;
    }

    public void markAsNotified(Date now) {
        if (status != WatchStatus.ACTIVE) {
            throw new IllegalStateException("notification.error.watch.notActive");
        }
        this.status = WatchStatus.NOTIFIED;
        this.notifiedAt = Objects.requireNonNull(now, "Notification time must not be null");
    }

    /** Idempotent: closing an already closed watch is a no-op. */
    public void close() {
        this.status = WatchStatus.CLOSED;
    }

    /** Whether the client was told the equipment is free no longer than {@code window} ago. */
    public boolean wasNotifiedWithin(Duration window, Date now) {
        return notifiedAt != null
                && !notifiedAt.toInstant().plus(window).isBefore(now.toInstant());
    }
}
