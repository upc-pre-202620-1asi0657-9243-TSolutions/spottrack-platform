package com.spottrack.platform.gym.domain.model.events;

import java.time.LocalDate;

public record MaintenanceThresholdReachedEvent(String equipmentId, LocalDate threshold) {}
