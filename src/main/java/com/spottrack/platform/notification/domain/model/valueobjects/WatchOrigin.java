package com.spottrack.platform.notification.domain.model.valueobjects;

/**
 * Why a client is following an equipment.
 */
public enum WatchOrigin {
    /** The client turned on the notification bell of an unavailable machine. */
    CLIENT_REQUEST,
    /** The client reported an anomaly on the machine and is affected by its repair. */
    ANOMALY_REPORT
}
