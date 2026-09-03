package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.commands.AddBranchCommand;
import com.spottrack.platform.gym.interfaces.rest.resources.AddBranchResource;

public class AddBranchCommandFromResourceAssembler {
    public static AddBranchCommand toCommandFromResource(String gymId, AddBranchResource resource) {
        return new AddBranchCommand( gymId,resource.name(), resource.address());
    }

    public static AddBranchResource toResourceFromCommand(String gymId, AddBranchCommand command){
        var resource = new AddBranchResource(command.name(), command.address());
        return resource;
    }
}


