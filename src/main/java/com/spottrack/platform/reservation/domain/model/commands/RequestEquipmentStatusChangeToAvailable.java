package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationRequestId;

/**
 * Command: signals that the equipment associated with this request should be released back to AVAILABLE.
 * Triggered either when the reservation timer expires or when the reservation ends.
 * Moves the ReservationRequest status to AVAILABLE_REQUESTED and notifies the Equipment bounded context.
 */
public record RequestEquipmentStatusChangeToAvailable(ReservationRequestId requestId) {

}
