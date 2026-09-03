package com.spottrack.platform.profiles.domain.model.queries;

public record GetClientByUserIdQuery(Long userId) {
    public GetClientByUserIdQuery {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("User id cannot be null or less than 1");
        }
    }
}
