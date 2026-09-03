package com.spottrack.platform.profiles.domain.model.queries;

public record GetAdminByUserIdQuery(Long userId) {
    public GetAdminByUserIdQuery {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("User id cannot be null or less than 1");
        }
    }
}
