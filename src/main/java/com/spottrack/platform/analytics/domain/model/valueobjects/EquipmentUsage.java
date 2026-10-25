package com.spottrack.platform.analytics.domain.model.valueobjects;

/**
 * @param utilizationRate % of the window's opening hours the equipment was in use
 * @param heat            activeMinutes relative to the busiest equipment in scope, 0..1
 */
public record EquipmentUsage(String equipmentId, String zoneId, double activeMinutes, int sessionCount,
                             double utilizationRate, double heat) {
}
