package com.spottrack.platform.reservation.application.internal.commandservices;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.commands.CancelReservation;
import com.spottrack.platform.reservation.domain.model.commands.CreateReservationFromRequest;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.commands.StartReservationTimer;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.reservation.domain.repositories.ReservationRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final GymContextFacade gymContextFacade;

    public ReservationCommandServiceImpl(ReservationRepository reservationRepository, GymContextFacade gymContextFacade) {
        this.reservationRepository = reservationRepository;
        this.gymContextFacade = gymContextFacade;
    }

    @Override
    public Result<Reservation, ApplicationError> handle(InitiateExpressReservation command) {
        try {
            var equipmentAvailabilityError = checkEquipmentAvailable(command.equipmentId().uuid());
            if (equipmentAvailabilityError.isPresent()) {
                return Result.failure(equipmentAvailabilityError.get());
            }
            if (reservationRepository.existsByEquipmentIdAndStatus(command.equipmentId().uuid(), ReservationStatus.ACTIVE)) {
                return Result.failure(ApplicationError.validationError("Reservation", "Equipment already has an active reservation"));
            }
            var reservation = new Reservation(command);
            var alreadyReserved = reservationRepository.existsByClientIdAndStatus(reservation.getClientId().clientId(), ReservationStatus.ACTIVE);
            if (alreadyReserved == true) {
                return Result.failure(ApplicationError.validationError("Reservation", "Client already has an active reservation"));
            }
            var savedReservation = reservationRepository.save(reservation);
            return Result.success(savedReservation);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Reservation", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Reservation creation", e.getMessage()));
        }
    }

    @Override
    public Result<Reservation, ApplicationError> handle(CreateReservationFromRequest command) {
        try {
            var equipmentAvailabilityError = checkEquipmentAvailable(command.equipmentId().uuid());
            if (equipmentAvailabilityError.isPresent()) {
                return Result.failure(equipmentAvailabilityError.get());
            }
            // Guard against the Express path double-firing this: express already
            // creates its own Reservation, then auto-submits a ReservationRequest as
            // a side effect (see ExpressReservationInitiatedEventHandler), whose
            // RequestOccupyEquipmentSubmittedEvent would otherwise land here too.
            if (reservationRepository.existsByEquipmentIdAndStatus(command.equipmentId().uuid(), ReservationStatus.ACTIVE)) {
                return Result.failure(ApplicationError.validationError("Reservation", "Equipment already has an active reservation"));
            }
            var reservation = new Reservation(command);
            var savedReservation = reservationRepository.save(reservation);
            return Result.success(savedReservation);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Reservation", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Reservation creation", e.getMessage()));
        }
    }

    private java.util.Optional<ApplicationError> checkEquipmentAvailable(String equipmentId) {
        var equipmentOpt = gymContextFacade.findEquipmentById(equipmentId);
        if (equipmentOpt.isEmpty()) {
            return java.util.Optional.of(ApplicationError.notFound("Equipment", equipmentId));
        }
        if (equipmentOpt.get().getStatus() != EquipmentStatus.AVAILABLE) {
            return java.util.Optional.of(ApplicationError.validationError("Reservation", "Equipment is not available for reservation"));
        }
        return java.util.Optional.empty();
    }

    @Override
    public Result<Reservation, ApplicationError> handle(CancelReservation command) {
        try {
            var found = reservationRepository.findByUuid(command.reservationId().uuid());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("Reservation", command.reservationId().uuid()));
            var domain = found.get();
            domain.cancel();
            return Result.success(reservationRepository.save(domain));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("Reservation", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Reservation cancellation", e.getMessage()));
        }
    }

    @Override
    public Result<Reservation, ApplicationError> handle(StartReservationTimer command) {
        try {
            var found = reservationRepository.findByUuid(command.reservationId().uuid());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("Reservation", command.reservationId().uuid()));
            var domain = found.get();
            domain.startTimer(command.durationMinutes());
            return Result.success(reservationRepository.save(domain));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("Reservation", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Reservation timer start", e.getMessage()));
        }
    }

    @Override
    public Result<Reservation, ApplicationError> handle(EndReservation command) {
        try {
            var found = reservationRepository.findByUuid(command.reservationId().uuid());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("Reservation", command.reservationId().uuid()));
            var domain = found.get();
            domain.end();
            return Result.success(reservationRepository.save(domain));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("Reservation", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Reservation end", e.getMessage()));
        }
    }
}