package com.spottrack.platform.reservation.domain.model.events;

import java.time.LocalDateTime;

/**
 * Published when the timer begins on an active reservation.
 * A scheduler or policy can listen to this and fire RequestEquipmentStatusChangeToAvailable
 * once timerExpiry is reached.
 */
public record ReservationTimerStartedEvent(String reservationId, String equipmentId, LocalDateTime timerExpiry) {
}
