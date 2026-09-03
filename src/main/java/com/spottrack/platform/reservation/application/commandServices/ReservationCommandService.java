package com.spottrack.platform.reservation.application.commandServices;

import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.commands.CancelReservation;
import com.spottrack.platform.reservation.domain.model.commands.CreateReservationFromRequest;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.commands.StartReservationTimer;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

/**
 * Contract for all Reservation aggregate commands.
 * Implementations live in application.internal and should not be exposed outside this package.
 */
public interface ReservationCommandService {

    Result<Reservation, ApplicationError> handle(InitiateExpressReservation command);

    Result<Reservation, ApplicationError> handle(CreateReservationFromRequest command);

    Result<Reservation, ApplicationError> handle(CancelReservation command);

    Result<Reservation, ApplicationError> handle(StartReservationTimer command);

    Result<Reservation, ApplicationError> handle(EndReservation command);

}
