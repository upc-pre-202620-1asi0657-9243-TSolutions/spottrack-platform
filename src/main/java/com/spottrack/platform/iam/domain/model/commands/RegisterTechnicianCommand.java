package com.spottrack.platform.iam.domain.model.commands;

public record RegisterTechnicianCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        Long adminUserId) {
}
