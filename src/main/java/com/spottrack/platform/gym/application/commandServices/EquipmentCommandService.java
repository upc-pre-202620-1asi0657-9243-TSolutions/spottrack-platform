package com.spottrack.platform.gym.application.commandServices;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.commands.*;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface EquipmentCommandService {
   public Result<Equipment, ApplicationError> handle(RegisterEquipment command);
   public Result<Equipment, ApplicationError> handle(MarkEquipmentOutOfService command);
   public Result<Equipment, ApplicationError> handle(UpdateEquipmentStatus command);
   public Result<Equipment, ApplicationError> handle(RelocateEquipment command);
   public Result<Equipment, ApplicationError> handle(DecomissionEquipment command);
   public Result<Equipment, ApplicationError> handle(DefineMaintenanceThresholdCommand command);
   public Result<Equipment, ApplicationError> handle(MarkMaintenanceThresholdReached command);
}
