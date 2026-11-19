package com.spottrack.platform.gym.infrastructure.messaging.stomp;

import java.time.Instant;

/**
 * Payload pushed to {@code /topic/gyms/{gymId}/equipment} whenever an equipment changes status.
 *
 * @param equipmentId the equipment whose status changed
 * @param status      the new status, as the {@code EquipmentStatus} enum name
 * @param changedAt   when the backend published the change, so clients can measure end-to-end latency
 */
public record EquipmentStatusMessage(String equipmentId, String status, Instant changedAt) {
}
