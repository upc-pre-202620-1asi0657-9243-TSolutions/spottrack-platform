package com.spottrack.platform.gym.domain.model.commands;

public record CreateGym(String gymName, Long adminUserId) {
    public CreateGym {
        if (gymName == null || gymName.isBlank())
            throw new IllegalArgumentException("gymName must not be null or blank");
        if (adminUserId == null || adminUserId == 0L)
            throw new IllegalArgumentException("adminUserId must not be null or zero");
    }
}
