package com.spottrack.platform.profiles.domain.model.valueobjects;

public record ClientId (Long clientId){
    public ClientId{
        if (clientId == null || clientId < 1){
            throw new IllegalArgumentException("Client id cannot be null or less than 1");
        }
    }
}
