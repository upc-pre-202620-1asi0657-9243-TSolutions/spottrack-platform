package com.spottrack.platform.analytics.interfaces.rest.resources;

import java.time.LocalDate;
import java.util.List;

public record UsageHeatmapResource(LocalDate from, LocalDate to, int openingHour, int closingHour, int equipmentCount,
                                   double occupancyRate, double previousOccupancyRate,
                                   Integer peakHour, double peakOccupancyRate,
                                   List<HourlyOccupancyResource> hourly, List<EquipmentUsageResource> equipment,
                                   List<DailyUsageResource> daily,
                                   int inactiveEquipmentCount, int previousInactiveEquipmentCount) {

    public record HourlyOccupancyResource(int hour, double occupancyRate) {
    }

    /** @param dayOfWeek MONDAY..SUNDAY */
    public record DailyUsageResource(String dayOfWeek, double activeMinutes, double previousActiveMinutes) {
    }

    public record EquipmentUsageResource(String equipmentId, String zoneId, double activeMinutes, int sessionCount,
                                         double utilizationRate, double heat) {
    }
}
