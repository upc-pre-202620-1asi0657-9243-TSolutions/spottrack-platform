package com.spottrack.platform.reservation.interfaces.rest.transform;

import com.spottrack.platform.reservation.domain.model.commands.RequestAlternativeEquipment;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationRequestId;
import com.spottrack.platform.reservation.interfaces.rest.resources.RequestAlternativeEquipmentResource;

/**
 * The requestId comes from the URL path variable, not from the request body.
 * The assembler merges both to build the complete command.
 */
public class RequestAlternativeEquipmentCommandFromResourceAssembler {

    public static RequestAlternativeEquipment toCommandFromResource(String requestId, RequestAlternativeEquipmentResource resource) {
        return new RequestAlternativeEquipment(new ReservationRequestId(requestId), resource.reason());
    }
}
