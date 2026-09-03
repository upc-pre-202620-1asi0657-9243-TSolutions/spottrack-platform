package com.spottrack.platform.profiles.interfaces.events;

public record ClientRegisteredIntegrationEvent(
        Long clientId,
        Long userId,
        String firstName,
        String lastName,
        String email
) {
}
