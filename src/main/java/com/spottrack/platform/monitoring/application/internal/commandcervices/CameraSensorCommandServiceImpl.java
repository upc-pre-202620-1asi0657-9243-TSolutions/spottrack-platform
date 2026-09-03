package com.spottrack.platform.monitoring.application.internal.commandcervices;

import com.spottrack.platform.monitoring.application.commandServices.CameraSensorCommandService;
import com.spottrack.platform.monitoring.domain.model.aggregates.CameraSensor;
import com.spottrack.platform.monitoring.domain.model.commands.RegisterCameraSensorCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.repositories.CameraSensorRepository;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class CameraSensorCommandServiceImpl implements CameraSensorCommandService {
    private final CameraSensorRepository cameraSensorRepository;
    private final GymContextFacade gymContextFacade;

    public CameraSensorCommandServiceImpl(CameraSensorRepository cameraSensorRepository, GymContextFacade gymContextFacade) {
        this.cameraSensorRepository = cameraSensorRepository;
        this.gymContextFacade = gymContextFacade;
    }

    @Override
    public Result<CameraSensor, ApplicationError> handle(RegisterCameraSensorCommand command) {
        try {
            if (gymContextFacade.findEquipmentById(command.equipmentId()).isEmpty()) {
                return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId()));
            }
            if (cameraSensorRepository.existsByEquipmentId(new EquipmentId(command.equipmentId()))) {
                return Result.failure(ApplicationError.conflict(
                        "CameraSensor",
                        "A camera sensor is already registered for equipment '%s'".formatted(command.equipmentId())
                ));
            }
            var cameraSensor = new CameraSensor(command);
            var saved = cameraSensorRepository.save(cameraSensor);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("CameraSensor", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Camera sensor registration", e.getMessage()));
        }
    }
}
