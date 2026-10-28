package com.spottrack.platform.reservation.application.internal.commandservices;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.commands.CancelReservation;
import com.spottrack.platform.reservation.domain.model.commands.CreateReservationFromRequest;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.commands.ExpireReservation;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.commands.StartReservationTimer;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.reservation.domain.repositories.ReservationRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReservationCommandServiceImpl implements ReservationCommandService {

    static final String EQUIPMENT_ALREADY_RESERVED = "Equipment is already reserved";
    static final String CLIENT_ALREADY_HAS_RESERVATION = "Client already has an active reservation";
    static final String EQUIPMENT_NOT_AVAILABLE = "Equipment is not available";

    private final ReservationRepository reservationRepository;
    private final GymContextFacade gymContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final Clock clock;
    private final int expressHoldMinutes;

    public ReservationCommandServiceImpl(ReservationRepository reservationRepository,
                                         GymContextFacade gymContextFacade,
                                         ProfilesContextFacade profilesContextFacade,
                                         Clock clock,
                                         @Value("${spottrack.reservation.express-hold-minutes:10}") int expressHoldMinutes) {
        this.reservationRepository = reservationRepository;
        this.gymContextFacade = gymContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.clock = clock;
        this.expressHoldMinutes = expressHoldMinutes;
    }

    @Override
    public Result<Reservation, ApplicationError> handle(InitiateExpressReservation command) {
        try {
            var equipmentId = command.equipmentId().uuid();
            var clientId = command.clientId().clientId();
            var now = LocalDateTime.now(clock);

            var equipmentOpt = gymContextFacade.findEquipmentById(equipmentId);
            if (equipmentOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", equipmentId));
            }
            var gymError = checkEquipmentBelongsToClientGym(clientId, equipmentId);
            if (gymError.isPresent()) {
                return Result.failure(gymError.get());
            }

            var expiredAny = expireStaleHoldOnEquipment(equipmentId, now);
            expiredAny |= expireStaleHoldsOfClient(clientId, now);
            if (expiredAny) {
                equipmentOpt = gymContextFacade.findEquipmentById(equipmentId);
                if (equipmentOpt.isEmpty()) {
                    return Result.failure(ApplicationError.notFound("Equipment", equipmentId));
                }
            }

            var status = equipmentOpt.get().getStatus();
            if (status == EquipmentStatus.OCCUPIED || status == EquipmentStatus.RESERVED) {
                return Result.failure(ApplicationError.conflict("Reservation", EQUIPMENT_ALREADY_RESERVED));
            }
            if (status != EquipmentStatus.AVAILABLE) {
                return Result.failure(ApplicationError.businessRuleViolation("Reservation", EQUIPMENT_NOT_AVAILABLE));
            }
            if (reservationRepository.existsByEquipmentIdAndStatus(equipmentId, ReservationStatus.ACTIVE)) {
                return Result.failure(ApplicationError.conflict("Reservation", EQUIPMENT_ALREADY_RESERVED));
            }
            if (reservationRepository.existsByClientIdAndStatus(clientId, ReservationStatus.ACTIVE)) {
                return Result.failure(ApplicationError.conflict("Reservation", CLIENT_ALREADY_HAS_RESERVATION));
            }

            var reservation = new Reservation(command, now, expressHoldMinutes);
            try {
                return Result.success(reservationRepository.save(reservation));
            } catch (DataIntegrityViolationException e) {
                return Result.failure(resolveConcurrentConflict(equipmentId, clientId));
            }
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Reservation", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Reservation creation", e.getMessage()));
        }
    }

    private Optional<ApplicationError> checkEquipmentBelongsToClientGym(Long clientId, String equipmentId) {
        var clientGymId = profilesContextFacade.fetchActiveGymIdByClientId(clientId);
        var equipmentGymId = gymContextFacade.resolveGymIdForEquipment(equipmentId);
        if (clientGymId == null || clientGymId.isBlank() || equipmentGymId.isEmpty() || !clientGymId.equals(equipmentGymId.get())) {
            return Optional.of(ApplicationError.forbidden("Equipment", "equipmentId:" + equipmentId));
        }
        return Optional.empty();
    }

    private boolean expireStaleHoldOnEquipment(String equipmentId, LocalDateTime now) {
        return reservationRepository.findByEquipmentIdAndStatus(equipmentId, ReservationStatus.ACTIVE)
                .filter(r -> r.isHoldExpiredAt(now))
                .map(this::expireAndSave)
                .orElse(false);
    }

    private boolean expireStaleHoldsOfClient(Long clientId, LocalDateTime now) {
        var expired = false;
        for (var reservation : reservationRepository.findByClientId(clientId)) {
            if (reservation.isHoldExpiredAt(now)) {
                expired |= expireAndSave(reservation);
            }
        }
        return expired;
    }

    private boolean expireAndSave(Reservation reservation) {
        try {
            reservation.expire(LocalDateTime.now(clock));
        } catch (IllegalStateException e) {
            return false;
        }
        reservationRepository.save(reservation);
        return true;
    }

    private ApplicationError resolveConcurrentConflict(String equipmentId, Long clientId) {
        if (reservationRepository.existsByClientIdAndStatus(clientId, ReservationStatus.ACTIVE)
                && !reservationRepository.existsByEquipmentIdAndStatus(equipmentId, ReservationStatus.ACTIVE)) {
            return ApplicationError.conflict("Reservation", CLIENT_ALREADY_HAS_RESERVATION);
        }
        return ApplicationError.conflict("Reservation", EQUIPMENT_ALREADY_RESERVED);
    }

    @Override
    public Result<Reservation, ApplicationError> handle(CreateReservationFromRequest command) {
        try {
            var equipmentAvailabilityError = checkEquipmentAvailable(command.equipmentId().uuid());
            if (equipmentAvailabilityError.isPresent()) {
                return Result.failure(equipmentAvailabilityError.get());
            }
            // Never create a second ACTIVE reservation for the same equipment.
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
            domain.startTimer(command.durationMinutes(), LocalDateTime.now(clock));
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

    @Override
    public Result<Reservation, ApplicationError> handle(ExpireReservation command) {
        try {
            var found = reservationRepository.findByUuid(command.reservationId().uuid());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("Reservation", command.reservationId().uuid()));
            var domain = found.get();
            domain.expire(LocalDateTime.now(clock));
            return Result.success(reservationRepository.save(domain));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("Reservation", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Reservation expiry", e.getMessage()));
        }
    }
}
