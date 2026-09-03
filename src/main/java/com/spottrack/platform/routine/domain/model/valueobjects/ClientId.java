package com.spottrack.platform.routine.domain.model.valueobjects;

public record ClientId(Long clientId) {
    public ClientId {
        if (clientId == null || clientId <= 0) {
            throw new IllegalArgumentException("Client Id must not be null or positive");
        }
    }
}
