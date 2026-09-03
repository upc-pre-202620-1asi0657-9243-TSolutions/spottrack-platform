package com.spottrack.platform.monitoring.application.internal.commandcervices;

import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.commands.MarkMotionSensorDisconnectedCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MarkMotionSensorReconnectedCommand;
import com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MotionSensorCommandServiceImpl implements MotionSensorCommandService {
    private final MotionSensorRepository motionSensorRepository;
    private final GymContextFacade gymContextFacade;

    public MotionSensorCommandServiceImpl(MotionSensorRepository motionSensorRepository, GymContextFacade gymContextFacade) {
        this.motionSensorRepository = motionSensorRepository;
        this.gymContextFacade = gymContextFacade;
    }

    @Override
    public Result<MotionSensor, ApplicationError> handle(RegisterMotionSensorCommand command) {
        try {
            if (gymContextFacade.findEquipmentById(command.equipmentId()).isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId()));
            }
            if (motionSensorRepository.existsByEquipmentId(new EquipmentId(command.equipmentId()))) {
                return Result.failure(ApplicationError.conflict(
                        "MotionSensor",
                        "A motion sensor is already registered for equipment '%s'".formatted(command.equipmentId())
                ));
            }
            var motionSensor = new MotionSensor(command);
            var saved = motionSensorRepository.save(motionSensor);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("MotionSensor", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Motion sensor registration", e.getMessage()));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Result<MotionSensor, ApplicationError> handle(MarkMotionSensorDisconnectedCommand command) {
        try {
            var found = motionSensorRepository.findById(command.id());
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("MotionSensor", command.id().toString()));
            }
            var sensor = found.get();
            sensor.markDisconnected();
            var saved = motionSensorRepository.save(sensor);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Motion sensor disconnection", e.getMessage()));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Result<MotionSensor, ApplicationError> handle(MarkMotionSensorReconnectedCommand command) {
        try {
            var found = motionSensorRepository.findById(command.id());
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("MotionSensor", command.id().toString()));
            }
            var sensor = found.get();
            sensor.markReconnected();
            var saved = motionSensorRepository.save(sensor);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Motion sensor reconnection", e.getMessage()));
        }
    }
}
