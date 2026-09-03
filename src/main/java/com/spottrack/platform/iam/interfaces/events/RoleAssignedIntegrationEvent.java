package com.spottrack.platform.iam.interfaces.events;

public record RoleAssignedIntegrationEvent(
        Long userId,
        String email,
        String role,
        String firstName,
        String lastName,
        String phoneNumber,
        String dni) {
}
