package com.spottrack.platform.gym.application.internal.commandservices;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.commands.*;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.repositories.EquipmentRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentCommandServiceImpl implements EquipmentCommandService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentCommandServiceImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional
    @Override
    public Result<Equipment, ApplicationError> handle(RegisterEquipment command) {
        try {
            var equipment = new Equipment(command);
            var saved = equipmentRepository.save(equipment);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Equipment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Equipment registration", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Equipment, ApplicationError> handle(MarkEquipmentOutOfService command) {
        try {
            var found = equipmentRepository.findById(command.id());
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", command.id().uuid()));
            }
            var equipment = found.get();
            equipment.markEquipmentOutOfService();
            equipmentRepository.save(equipment);
            return Result.success(equipment);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Equipment", e.getMessage()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.conflict("Equipment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Equipment mark out of service", e.getMessage()));
        }
    }

    /**
     * REQUIRES_NEW: this command is invoked (via the various cross-context
     * integration event handlers) from @TransactionalEventListener(AFTER_COMMIT)
     * callbacks, i.e. after the triggering transaction has already committed.
     * Joining that transaction's (already-completing) context instead of
     * starting a fresh one means the write can end up not being flushed
     * until some unrelated later transaction happens to touch the session.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Result<Equipment, ApplicationError> handle(UpdateEquipmentStatus command) {
        try {
            var found = equipmentRepository.findById(new EquipmentId(command.id()));
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", command.id()));
            }
            var equipment = found.get();
            equipment.updateStatus(command.status());
            equipmentRepository.save(equipment);
            return Result.success(equipment);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Equipment", e.getMessage()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.conflict("Equipment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Equipment status update", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Equipment, ApplicationError> handle(RelocateEquipment command) {
        try {
            var found = equipmentRepository.findById(command.equipmentId());
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId().uuid()));
            }
            var equipment = found.get();
            equipment.relocateEquipment(command.zoneId());
            equipmentRepository.save(equipment);
            return Result.success(equipment);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Equipment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Equipment relocation", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Equipment, ApplicationError> handle(DecomissionEquipment command) {
        try {
            var found = equipmentRepository.findById(command.equipmentId());
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId().uuid()));
            }
            var equipment = found.get();
            equipment.updateStatus(command.equipmentStatus());
            equipmentRepository.save(equipment);
            return Result.success(equipment);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Equipment", e.getMessage()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.conflict("Equipment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Equipment decommission", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Equipment, ApplicationError> handle(DefineMaintenanceThresholdCommand command) {
        try {
            var found = equipmentRepository.findById(command.equipmentId());
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId().uuid()));
            }
            var equipment = found.get();
            equipment.defineMaintenanceThreshold(command.threshold());
            equipmentRepository.save(equipment);
            return Result.success(equipment);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Equipment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Equipment maintenance threshold", e.getMessage()));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Result<Equipment, ApplicationError> handle(MarkMaintenanceThresholdReached command) {
        try {
            var found = equipmentRepository.findById(command.equipmentId());
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId().uuid()));
            }
            var equipment = found.get();
            equipment.markMaintenanceThresholdReached();
            equipmentRepository.save(equipment);
            return Result.success(equipment);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Equipment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Equipment maintenance threshold reached", e.getMessage()));
        }
    }
}
