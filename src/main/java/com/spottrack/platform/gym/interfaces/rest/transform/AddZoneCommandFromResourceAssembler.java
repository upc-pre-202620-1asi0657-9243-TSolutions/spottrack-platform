package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.commands.AddZoneCommand;
import com.spottrack.platform.gym.domain.model.valueobjects.BranchId;
import com.spottrack.platform.gym.interfaces.rest.resources.AddZoneResource;

public class AddZoneCommandFromResourceAssembler {
    public static AddZoneCommand toCommandFromResource(String branchId, AddZoneResource resource) {
        return new AddZoneCommand(resource.zoneName(), resource.maximumOccupancy(), new BranchId(branchId));
    }
}
