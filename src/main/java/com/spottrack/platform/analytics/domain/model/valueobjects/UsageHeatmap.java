package com.spottrack.platform.analytics.domain.model.valueobjects;

import java.time.LocalDate;
import java.util.List;

/**
 * Read model built from UsageSession history.
 *
 * @param occupancyRate         average % of equipment in use during opening hours
 * @param previousOccupancyRate the same figure for the window of equal length right before
 * @param peakHour              hour of the day with the highest occupancy, null without usage
 * @param daily                 minutes of use per day of the week, Monday first, with the previous window's figure
 * @param inactiveEquipmentCount         equipment with no use at all in the window
 * @param previousInactiveEquipmentCount the same figure for the window of equal length right before
 */
public record UsageHeatmap(LocalDate from, LocalDate to, int openingHour, int closingHour, int equipmentCount,
                           double occupancyRate, double previousOccupancyRate,
                           Integer peakHour, double peakOccupancyRate,
                           List<HourlyOccupancy> hourly, List<EquipmentUsage> equipment,
                           List<DailyUsage> daily, int inactiveEquipmentCount, int previousInactiveEquipmentCount) {
}
