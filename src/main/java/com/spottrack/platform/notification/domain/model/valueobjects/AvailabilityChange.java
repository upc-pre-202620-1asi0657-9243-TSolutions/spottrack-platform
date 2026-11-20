package com.spottrack.platform.notification.domain.model.valueobjects;

/**
 * How a change of equipment status affects the clients that follow the equipment.
 */
public enum AvailabilityChange {
    /** The machine was in use or reserved and is free now. */
    RELEASED,
    /** The machine was under maintenance or out of service and is operational again. */
    RESTORED,
    /** The machine was taken (occupied or reserved). */
    TAKEN,
    /** The machine went into maintenance or out of service. */
    BLOCKED,
    /** The machine was decommissioned and will never be available again. */
    RETIRED
}
