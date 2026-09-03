package com.spottrack.platform.gym.application.commandServices;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.commands.AddBranchCommand;
import com.spottrack.platform.gym.domain.model.commands.AddDniToWhitelistCommand;
import com.spottrack.platform.gym.domain.model.commands.AddZoneCommand;
import com.spottrack.platform.gym.domain.model.commands.CreateGym;
import com.spottrack.platform.gym.domain.model.commands.RemoveDniFromWhitelistCommand;
import com.spottrack.platform.gym.domain.model.commands.RequestEquipmentRelocation;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.domain.model.entities.GymWhitelistEntry;
import com.spottrack.platform.gym.domain.model.entities.Zone;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface GymCommandService {
    public Result<Equipment, ApplicationError> handle(RequestEquipmentRelocation command);
    public Result<Gym, ApplicationError> handle(CreateGym command);
    public Result<Branch, ApplicationError> handle(AddBranchCommand command);
    public Result<Zone, ApplicationError> handle(AddZoneCommand command);
    public Result<GymWhitelistEntry, ApplicationError> handle(AddDniToWhitelistCommand command);
    public void handle(RemoveDniFromWhitelistCommand command);
}
