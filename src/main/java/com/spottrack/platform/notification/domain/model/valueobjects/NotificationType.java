package com.spottrack.platform.notification.domain.model.valueobjects;

/**
 * Kinds of in-app notifications a client can receive about an equipment.
 */
public enum NotificationType {
    /** A machine the client was waiting for has been released. */
    EQUIPMENT_AVAILABLE,
    /** A machine that was under maintenance or out of service is operational again. */
    EQUIPMENT_RESTORED,
    /** A machine the client was told is free was taken by someone else before they got to it. */
    EQUIPMENT_OCCUPIED_AGAIN
}
