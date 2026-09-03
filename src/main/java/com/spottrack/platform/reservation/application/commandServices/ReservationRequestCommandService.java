package com.spottrack.platform.reservation.application.commandServices;

import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;
import com.spottrack.platform.reservation.domain.model.commands.RequestAlternativeEquipment;
import com.spottrack.platform.reservation.domain.model.commands.RequestEquipmentStatusChangeToAvailable;
import com.spottrack.platform.reservation.domain.model.commands.SubmitRequestOccupyEquipment;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

/**
 * Contract for all ReservationRequest aggregate commands.
 */
public interface ReservationRequestCommandService {

    Result<ReservationRequest, ApplicationError> handle(SubmitRequestOccupyEquipment command);

    Result<ReservationRequest, ApplicationError> handle(RequestAlternativeEquipment command);

    Result<ReservationRequest, ApplicationError> handle(RequestEquipmentStatusChangeToAvailable command);
}
