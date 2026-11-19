package com.spottrack.platform.analytics.domain.model.valueobjects;

import java.time.DayOfWeek;

/**
 * Minutes of real equipment use that fell on one day of the week, added up across every
 * such day of the window.
 *
 * @param activeMinutes         minutes of use on this weekday inside the requested window
 * @param previousActiveMinutes the same figure for the window of equal length right before
 */
public record DailyUsage(DayOfWeek dayOfWeek, double activeMinutes, double previousActiveMinutes) {
}
