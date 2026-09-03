package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationRequestId;

/**
 * Command: the client is not satisfied with the assigned equipment and asks for an alternative.
 * Moves the ReservationRequest status to ALTERNATIVE_REQUESTED.
 * The reason field is optional but helpful for staff to act on.
 */
public record RequestAlternativeEquipment(ReservationRequestId requestId, String reason) {

}
