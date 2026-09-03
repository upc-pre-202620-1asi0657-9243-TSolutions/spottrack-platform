package com.spottrack.platform.profiles.domain.model.commands;

import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;

public record CreateClientCommand(
        Long userId,
        EmailAddress emailAddress
) { }
