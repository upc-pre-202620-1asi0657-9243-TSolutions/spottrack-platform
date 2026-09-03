package com.spottrack.platform.routine.interfaces.rest.transform;

import com.spottrack.platform.routine.domain.model.commands.CreateRoutineCommand;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineName;
import com.spottrack.platform.routine.interfaces.rest.resources.CreateRoutineResource;

public class CreateRoutineCommandFromResourceAssembler {

    public static CreateRoutineCommand toCommandFromResource(CreateRoutineResource resource, Long clientId) {
        return new CreateRoutineCommand(
                new RoutineName(resource.routineName()),
                new ClientId(clientId));
    }
}
