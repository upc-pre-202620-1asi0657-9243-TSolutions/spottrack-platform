package com.spottrack.platform.notification.domain.model.valueobjects;

/**
 * Lifecycle of an equipment watch.
 *
 * ACTIVE   → waiting for the equipment to become available
 * NOTIFIED → the client was told the equipment is available; tracked until someone takes it
 * CLOSED   → no longer tracked (cancelled by the client, taken, or the equipment was retired)
 */
public enum WatchStatus {
    ACTIVE,
    NOTIFIED,
    CLOSED
}
