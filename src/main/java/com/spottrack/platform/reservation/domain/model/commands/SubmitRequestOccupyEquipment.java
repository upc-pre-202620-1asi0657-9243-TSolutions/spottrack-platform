package com.spottrack.platform.reservation.domain.model.commands;

import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;

/**
 * Command: a client submits a formal request to occupy a specific piece of equipment.
 * Triggers the event: RequestOccupyEquipmentSubmitted, which notifies the Equipment bounded context
 * to mark the equipment as RESERVED.
 *
 * equipmentId → string reference to the Equipment aggregate (cross-context, no object reference)
 * clientId    → identifies who is making the request
 */
    public record SubmitRequestOccupyEquipment(ClientId clientId, EquipmentId equipmentId) {
}
