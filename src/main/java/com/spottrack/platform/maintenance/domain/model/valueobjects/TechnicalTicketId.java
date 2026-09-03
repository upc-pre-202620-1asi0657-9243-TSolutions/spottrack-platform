package com.spottrack.platform.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record TechnicalTicketId(String uuid) {

    public TechnicalTicketId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("maintenance.error.technicalTicketId.notBlank");
        }
    }
}
