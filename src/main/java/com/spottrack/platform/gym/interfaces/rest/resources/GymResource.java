package com.spottrack.platform.gym.interfaces.rest.resources;

public record GymResource(
        String gymId,
        String name,
        Long adminUserId
) {}
