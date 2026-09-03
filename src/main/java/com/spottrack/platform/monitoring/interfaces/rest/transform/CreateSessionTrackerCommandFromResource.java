package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.domain.model.commands.CreateSessionTrackerCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.UsageActivity;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CreateSessionTrackerResource;

public class CreateSessionTrackerCommandFromResource {
    public static CreateSessionTrackerCommand toCommandFromResource(CreateSessionTrackerResource resource){
        return new CreateSessionTrackerCommand(
                new SessionTrackerId(resource.sessionTrackerId()),
                new EquipmentId(resource.equipmentId()),
                resource.reservationId() != null ? new ReservationId(resource.reservationId()) : null,
                resource.sessionIsActive(),
                resource.sessionIsInactive(),
                new UsageActivity(resource.continuousActivity(), resource.seconds())
        );
    }


}
