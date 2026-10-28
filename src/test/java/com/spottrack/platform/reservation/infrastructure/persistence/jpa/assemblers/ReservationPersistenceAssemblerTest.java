package com.spottrack.platform.reservation.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.commands.CreateReservationFromRequest;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReservationPersistenceAssemblerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 27, 18, 30, 0);

    private static Reservation express() {
        return new Reservation(new InitiateExpressReservation(new ClientId(5L), new EquipmentId("eq-1"), null, null), NOW, 10);
    }

    @Test
    void activeExpressHoldPopulatesBothUniqueKeysAndTheHoldDeadline() {
        var entity = ReservationPersistenceAssembler.toPersistenceFromDomain(express());

        assertEquals("eq-1", entity.getActiveEquipmentKey());
        assertEquals(5L, entity.getActiveClientKey());
        assertEquals(NOW.plusMinutes(10), entity.getHoldExpiresAt());
    }

    @Test
    void activeReservationFromRequestOnlyGuardsTheEquipment() {
        var reservation = new Reservation(new CreateReservationFromRequest(new ClientId(5L), new EquipmentId("eq-1")));

        var entity = ReservationPersistenceAssembler.toPersistenceFromDomain(reservation);

        assertEquals("eq-1", entity.getActiveEquipmentKey());
        assertNull(entity.getActiveClientKey());
        assertNull(entity.getHoldExpiresAt());
    }

    @Test
    void keysAreClearedOnceTheReservationIsNoLongerActive() {
        var reservation = express();
        reservation.expire(NOW.plusMinutes(10));

        var entity = ReservationPersistenceAssembler.toPersistenceFromDomain(reservation);

        assertNull(entity.getActiveEquipmentKey());
        assertNull(entity.getActiveClientKey());
    }

    @Test
    void holdDeadlineSurvivesARoundTrip() {
        var entity = ReservationPersistenceAssembler.toPersistenceFromDomain(express());

        var restored = ReservationPersistenceAssembler.toDomainFromPersistence(entity);

        assertEquals(NOW.plusMinutes(10), restored.getHoldExpiresAt());
    }
}
