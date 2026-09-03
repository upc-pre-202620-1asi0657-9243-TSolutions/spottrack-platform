package com.spottrack.platform.reservation.interfaces.rest.transform;

import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.reservation.domain.model.valueobjects.TimeInterval;
import com.spottrack.platform.reservation.interfaces.rest.resources.InitiateExpressReservationResource;

import java.sql.Time;

public class InitiateExpressReservationCommandFromResourceAssembler {

    public static InitiateExpressReservation toCommandFromResource(InitiateExpressReservationResource resource, Long clientId) {
        return new InitiateExpressReservation(
                new ClientId(clientId),
                new EquipmentId(resource.equipmentId()),
                new TimeInterval(Time.valueOf(resource.startTime()), Time.valueOf(resource.endTime())));
    }
}
