package com.spottrack.platform.monitoring.interfaces.rest.transform;


import com.spottrack.platform.monitoring.domain.model.commands.ReportAnomalyCommand;
import com.spottrack.platform.monitoring.interfaces.rest.resources.ReportAnomalyCommandResource;

public class ReportAnomalyCommandFromResource {
    public static ReportAnomalyCommand toCommandFromResource(ReportAnomalyCommandResource resource){
        return new ReportAnomalyCommand(resource.reservationId(), resource.equipmentId(), resource.zoneId(), resource.anomalyDescription());
    }
}
