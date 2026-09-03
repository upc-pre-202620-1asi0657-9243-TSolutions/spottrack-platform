package com.spottrack.platform.shared.interfaces.rest.transform;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.interfaces.rest.resources.AlertResource;

public class AlertResourceFromEntityAssembler {

    public static AlertResource toResourceFromEntity(Alert alert) {
        return new AlertResource(
                alert.getId(),
                alert.getEquipmentId(),
                alert.getSeverity().name(),
                alert.getMessage(),
                alert.isResolved(),
                alert.getCreatedAt()
        );
    }
}
