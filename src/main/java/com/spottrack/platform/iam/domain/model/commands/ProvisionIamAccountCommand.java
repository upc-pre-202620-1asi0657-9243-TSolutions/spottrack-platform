package com.spottrack.platform.iam.domain.model.commands;

import com.spottrack.platform.iam.domain.model.entities.Role;

import java.util.List;

public record ProvisionIamAccountCommand(
        String username,
        String alreadyHashedPassword,
        List<Role> roles
) {
    public ProvisionIamAccountCommand {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username must not be blank");
        if (alreadyHashedPassword == null || alreadyHashedPassword.isBlank())
            throw new IllegalArgumentException("Hashed password must not be blank");
        if (roles == null || roles.isEmpty())
            throw new IllegalArgumentException("Roles must not be empty");
    }
}
