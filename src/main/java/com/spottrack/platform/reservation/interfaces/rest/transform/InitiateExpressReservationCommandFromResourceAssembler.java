package com.spottrack.platform.reservation.interfaces.rest.transform;

import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.reservation.interfaces.rest.resources.InitiateExpressReservationResource;

import java.sql.Time;
import java.time.LocalTime;

public class InitiateExpressReservationCommandFromResourceAssembler {

    public static InitiateExpressReservation toCommandFromResource(InitiateExpressReservationResource resource, Long clientId) {
        return new InitiateExpressReservation(
                new ClientId(clientId),
                new EquipmentId(resource.equipmentId()),
                parseTime(resource.startTime()),
                parseTime(resource.endTime()));
    }

    private static Time parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Time.valueOf(LocalTime.parse(value.trim()));
    }
}
