package com.spottrack.platform.reservation.interfaces.acl;

import java.util.Optional;

public interface ReservationContextFacade {
    /**
     * Returns the clientId of the given reservation, or empty if no reservation with that uuid exists.
     */
    Optional<Long> fetchClientIdByReservationId(String reservationId);
}
