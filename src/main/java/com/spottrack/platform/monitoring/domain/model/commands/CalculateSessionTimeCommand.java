package com.spottrack.platform.monitoring.domain.model.commands;

import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;

import java.time.LocalTime;

/**
 *
 * @param sessionTrackerId
 */
public record CalculateSessionTimeCommand(SessionTrackerId sessionTrackerId) {
}
