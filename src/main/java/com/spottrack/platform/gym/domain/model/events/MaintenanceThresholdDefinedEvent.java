package com.spottrack.platform.gym.domain.model.events;

import java.time.LocalDate;

public record MaintenanceThresholdDefinedEvent(String equipmentId, LocalDate threshold) {}
