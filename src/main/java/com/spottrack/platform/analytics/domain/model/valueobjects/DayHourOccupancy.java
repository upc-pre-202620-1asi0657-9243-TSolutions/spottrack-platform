package com.spottrack.platform.analytics.domain.model.valueobjects;

import java.time.DayOfWeek;

/**
 * Occupancy of one hour on one day of the week, averaged across every such day of the window.
 *
 * @param occupancyRate % of equipment in use during that hour on that weekday
 */
public record DayHourOccupancy(DayOfWeek dayOfWeek, int hour, double occupancyRate) {
}
