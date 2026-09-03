package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.domain.model.aggregates.Anomaly;
import com.spottrack.platform.monitoring.interfaces.rest.resources.AnomalyResource;

public class AnomalyResourceFromEntity {
    public static AnomalyResource toResourceFromEntity(Anomaly entity){
        return new AnomalyResource(
                entity.getId(),
                entity.getAnomalyId().uuid(),
                entity.getReservationId().uuid(),
                entity.getEquipmentId().uuid(),
                entity.getZoneId().uuid(),
                entity.getAnomalyDescription(),
                entity.getEmissionDate()
        );
    }
}
