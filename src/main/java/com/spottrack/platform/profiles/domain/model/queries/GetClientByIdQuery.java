package com.spottrack.platform.profiles.domain.model.queries;

import com.spottrack.platform.profiles.domain.model.valueobjects.ClientId;

public record GetClientByIdQuery(ClientId clientId) {
    public GetClientByIdQuery{
        if (clientId == null || clientId.clientId() <= 0){
            throw new IllegalArgumentException("Client Id cannot be null or less than or equal to 0");
        }
    }
}
