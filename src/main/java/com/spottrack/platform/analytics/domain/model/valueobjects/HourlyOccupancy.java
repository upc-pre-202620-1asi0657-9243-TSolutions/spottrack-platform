package com.spottrack.platform.analytics.domain.model.valueobjects;

/** @param occupancyRate average % of equipment in use during this hour of the day */
public record HourlyOccupancy(int hour, double occupancyRate) {
}
