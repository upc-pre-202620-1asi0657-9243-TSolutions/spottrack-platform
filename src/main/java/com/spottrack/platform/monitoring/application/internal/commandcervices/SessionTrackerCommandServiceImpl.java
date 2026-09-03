package com.spottrack.platform.monitoring.application.internal.commandcervices;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.commands.*;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.UsageActivity;
import com.spottrack.platform.monitoring.domain.repositories.SessionTrackerRepository;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers.SessionTrackerPersistenceAssembler;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.UUID;

@Service
public class SessionTrackerCommandServiceImpl implements SessionTrackerCommandService {
    private final SessionTrackerRepository sessionTrackerRepository;
    private final GymContextFacade gymContextFacade;

    public SessionTrackerCommandServiceImpl(SessionTrackerRepository sessionTrackerRepository, GymContextFacade gymContextFacade){
        this.sessionTrackerRepository = sessionTrackerRepository;
        this.gymContextFacade = gymContextFacade;
    }

    /**
     * A sensor only knows what equipment it's mounted on — not a reservation.
     * Motion on equipment already being tracked feeds the existing tracker;
     * motion on equipment with no active tracker starts a walk-up session
     * (equipment used without a booked reservation) — real usage worth tracking.
     */
    private SessionTracker findOrCreateActiveTrackerForEquipment(EquipmentId equipmentId) {
        return sessionTrackerRepository.findActiveByEquipmentId(equipmentId)
                .orElseGet(() -> new SessionTracker(new CreateSessionTrackerCommand(
                        new SessionTrackerId(UUID.randomUUID().toString()),
                        equipmentId,
                        null,
                        true,
                        false,
                        new UsageActivity(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT)
                )));
    }
    @Transactional
    @Override
    public Result<SessionTracker, ApplicationError> handle(VerifyUsageSessionCommand command) {
        try {
            var session = sessionTrackerRepository.findSessionByUuid(command.sessionTrackerId());
            if (session.isEmpty()) {
                return Result.failure(ApplicationError.notFound("sessionTracker", command.sessionTrackerId().uuid()));
            }
            var tracker = session.get();
            tracker.verifyUsageSession();
            var saved = sessionTrackerRepository.save(tracker);
            return Result.success(saved);

        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("sessionTracker", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("sessionTracker", e.getMessage()));
        }
    }

    @Override
    public Result<SessionTracker, ApplicationError> handle(CreateSessionTrackerCommand command) {
      try {
          var entity = new SessionTracker(command);
          var saved = sessionTrackerRepository.save(entity);
          return Result.success(saved);
      }
      catch (IllegalArgumentException exception) {
          return Result.failure(ApplicationError.validationError("sessionTracker", exception.getMessage()));
      }
      catch (Exception e) {
          return Result.failure(ApplicationError.validationError("sessionTracker", e.getMessage()));
      }
    }

    @Override
    public Result<SessionTracker, ApplicationError> handle(MotionSensorCaptureCommand command) {
        if (gymContextFacade.findEquipmentById(command.equipmentId().uuid()).isEmpty()) {
            return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId().uuid()));
        }
        try {
            var tracker = findOrCreateActiveTrackerForEquipment(command.equipmentId());
            tracker.captureMotionSensorReading(command.movementDetectedViaSensor());
            var saved = sessionTrackerRepository.save(tracker);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("sessionTracker", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("sessionTracker", e.getMessage()));
        }
    }

    @Override
    public Result<SessionTracker, ApplicationError> handle(CameraCaptureMotionCommand command) {
        if (gymContextFacade.findEquipmentById(command.equipmentId().uuid()).isEmpty()) {
            return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId().uuid()));
        }
        try {
            var tracker = findOrCreateActiveTrackerForEquipment(command.equipmentId());
            tracker.captureCameraMotion(command.movementDetectedViaVideo());
            var saved = sessionTrackerRepository.save(tracker);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("sessionTracker", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("sessionTracker", e.getMessage()));
        }
    }

    /**
     * REQUIRES_NEW: this handler is invoked from an AFTER_COMMIT transactional event
     * listener, where the original transaction's resources are already tearing down.
     * Joining it (default propagation) silently no-ops the write, so a fresh
     * transaction is required here.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Override
    public Result<SessionTracker, ApplicationError> handle(EndUsageSessionCommand command) {
        /**
         * This command will retrieve a sessiontracker, and access its attributes and set one of the booleans to true
         */
        var entity = sessionTrackerRepository.findSessionByUuid(command.sessionTrackerId());
        if (entity.isEmpty()) {
            return Result.failure(ApplicationError.notFound("sessionTracker", command.sessionTrackerId().uuid()));
        }
        var session = entity.get();
        session.endSession();
        var patchedEntity = sessionTrackerRepository.save(session);
        return Result.success(patchedEntity);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Override
    public Result<SessionTracker, ApplicationError> handle(CalculateSessionTimeCommand command) {
        try {
            var session = sessionTrackerRepository.findSessionByUuid(command.sessionTrackerId());
            var tracker = session.get();
            tracker.calculateSessionTime();
            var saved = sessionTrackerRepository.save(tracker);
            return Result.success(saved);
        }
        catch (IllegalArgumentException e){
            return Result.failure(ApplicationError.validationError("sessionTracker", e.getMessage()));
        }

    }

    /**
     * A session tracker's job ends once its final activity data has been reported —
     * it's an ephemeral vehicle for that computation, not a record worth keeping.
     */
    @Transactional
    @Override
    public Result<Void, ApplicationError> handle(DeleteSessionTrackerCommand command) {
        try {
            sessionTrackerRepository.deleteBySessionTrackerId(command.sessionTrackerId());
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("sessionTracker", e.getMessage()));
        }
    }
}
