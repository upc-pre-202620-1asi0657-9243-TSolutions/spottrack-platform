package com.spottrack.platform.reservation.application.internal.commandservices;

import com.spottrack.platform.reservation.application.commandServices.ReservationRequestCommandService;
import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;
import com.spottrack.platform.reservation.domain.model.commands.RequestAlternativeEquipment;
import com.spottrack.platform.reservation.domain.model.commands.RequestEquipmentStatusChangeToAvailable;
import com.spottrack.platform.reservation.domain.model.commands.SubmitRequestOccupyEquipment;
import com.spottrack.platform.reservation.domain.repositories.ReservationRequestRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class ReservationRequestCommandServiceImpl implements ReservationRequestCommandService {

    private final ReservationRequestRepository reservationRequestRepository;

    public ReservationRequestCommandServiceImpl(ReservationRequestRepository reservationRequestRepository) {
        this.reservationRequestRepository = reservationRequestRepository;
    }

    @Override
    public Result<ReservationRequest, ApplicationError> handle(SubmitRequestOccupyEquipment command) {
        try {
            var request = new ReservationRequest(command);
            return Result.success(reservationRequestRepository.save(request));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("ReservationRequest", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("ReservationRequest creation", e.getMessage()));
        }
    }

    @Override
    public Result<ReservationRequest, ApplicationError> handle(RequestAlternativeEquipment command) {
        try {
            var found = reservationRequestRepository.findByUuid(command.requestId().uuid());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("ReservationRequest", command.requestId().uuid()));
            var request = found.get();
            request.requestAlternative(command);
            return Result.success(reservationRequestRepository.save(request));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("ReservationRequest", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("ReservationRequest alternative", e.getMessage()));
        }
    }

    @Override
    public Result<ReservationRequest, ApplicationError> handle(RequestEquipmentStatusChangeToAvailable command) {
        try {
            var found = reservationRequestRepository.findByUuid(command.requestId().uuid());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("ReservationRequest", command.requestId().uuid()));
            var request = found.get();
            request.requestEquipmentRelease(command);
            return Result.success(reservationRequestRepository.save(request));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("ReservationRequest", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("ReservationRequest release", e.getMessage()));
        }
    }
}
