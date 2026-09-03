package com.spottrack.platform.routine.interfaces.rest.transform;

import com.spottrack.platform.routine.domain.model.commands.StartRoutineCommand;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.interfaces.rest.resources.StartRoutineResource;

public class StartRoutineCommandFromResourceAssembler {

    public static StartRoutineCommand toCommandFromResource(StartRoutineResource resource, Long clientId) {
        return new StartRoutineCommand(
                resource.routineId(),
                new ClientId(clientId)
        );
    }
}
