package com.spottrack.platform.profiles.domain.model.commands;

import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;

public record CreateAdminCommand(
        Long userId,
        EmailAddress emailAddress,
        String firstName,
        String lastName,
        String phoneNumber,
        String dni
) {
    public CreateAdminCommand(Long userId, EmailAddress emailAddress) {
        this(userId, emailAddress, null, null, null, null);
    }
}
