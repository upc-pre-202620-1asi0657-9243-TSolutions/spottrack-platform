package com.spottrack.platform.maintenance.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceLogResource(String id, String ticketId, String maintenanceId, String notes, BigDecimal cost, LocalDateTime completedAt) {
}
