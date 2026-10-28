package com.spottrack.platform.reservation.interfaces.rest.transform;

import com.spottrack.platform.reservation.interfaces.rest.resources.InitiateExpressReservationResource;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InitiateExpressReservationCommandFromResourceAssemblerTest {

    @Test
    void onlyEquipmentIdIsEnough() {
        var command = InitiateExpressReservationCommandFromResourceAssembler
                .toCommandFromResource(new InitiateExpressReservationResource("eq-1", null, null), 5L);

        assertEquals("eq-1", command.equipmentId().uuid());
        assertNull(command.startTime());
        assertNull(command.endTime());
    }

    @Test
    void blankTimesAreTreatedAsMissing() {
        var command = InitiateExpressReservationCommandFromResourceAssembler
                .toCommandFromResource(new InitiateExpressReservationResource("eq-1", " ", ""), 5L);

        assertNull(command.startTime());
        assertNull(command.endTime());
    }

    @Test
    void sentTimesAreParsedWithOrWithoutSeconds() {
        var command = InitiateExpressReservationCommandFromResourceAssembler
                .toCommandFromResource(new InitiateExpressReservationResource("eq-1", "18:00", "18:45:30"), 5L);

        assertEquals(Time.valueOf("18:00:00"), command.startTime());
        assertEquals(Time.valueOf("18:45:30"), command.endTime());
    }

    @Test
    void malformedTimeIsRejected() {
        assertThrows(DateTimeParseException.class, () -> InitiateExpressReservationCommandFromResourceAssembler
                .toCommandFromResource(new InitiateExpressReservationResource("eq-1", "six pm", null), 5L));
    }

    @Test
    void missingEquipmentIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> InitiateExpressReservationCommandFromResourceAssembler
                .toCommandFromResource(new InitiateExpressReservationResource(null, null, null), 5L));
    }
}
