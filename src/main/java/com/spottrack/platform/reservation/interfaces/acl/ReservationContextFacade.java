package com.spottrack.platform.reservation.interfaces.acl;

import java.util.Optional;

public interface ReservationContextFacade {
    /**
     * Returns the clientId of the given reservation, or empty if no reservation with that uuid exists.
     */
    Optional<Long> fetchClientIdByReservationId(String reservationId);

    /**
     * Returns the clientId holding the active reservation of the given equipment, or empty if the
     * equipment has no active reservation (e.g. it is free or occupied by a walk-up session).
     */
    Optional<Long> fetchActiveReservationClientIdByEquipmentId(String equipmentId);
}
