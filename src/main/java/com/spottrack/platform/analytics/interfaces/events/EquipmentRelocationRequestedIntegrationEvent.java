package com.spottrack.platform.analytics.interfaces.events;

/**
 * Published when analytics recommends transferring equipment out of a
 * low-demand slot. Whoever owns actually acting on relocation requests
 * (notifying zone owners, moving equipment, etc.) should listen for this.
 */
public record EquipmentRelocationRequestedIntegrationEvent(Long roiProjectionId, String detail) {}
