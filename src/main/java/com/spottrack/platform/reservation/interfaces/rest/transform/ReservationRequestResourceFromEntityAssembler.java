package com.spottrack.platform.reservation.interfaces.rest.transform;

import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;
import com.spottrack.platform.reservation.interfaces.rest.resources.ReservationRequestResource;

/**
 * Converts the ReservationRequest aggregate into the HTTP response resource.
 */
public class ReservationRequestResourceFromEntityAssembler {

    public static ReservationRequestResource toResourceFromEntity(ReservationRequest request) {
        return new ReservationRequestResource(
                request.getId().uuid(),
                request.getClientId().clientId().toString(),
                request.getEquipmentId().uuid(),
                request.getStatus(),
                request.getRequestedAt()
        );
    }
}
