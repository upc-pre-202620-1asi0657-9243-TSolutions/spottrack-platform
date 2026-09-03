package com.spottrack.platform.profiles.interfaces.events;

import tools.jackson.databind.node.StringNode;

public record AdminRegisteredIntegrationEvent(
        Long clientId,
        Long userId,
        String firstName,
        String lastName,
        String email
) {
}
