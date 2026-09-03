package com.spottrack.platform.profiles.domain.model.commands;

import com.spottrack.platform.profiles.domain.model.valueobjects.ClientId;
import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;

public record UpdateClientProfileCommand(
        ClientId clientId,
        String firstName,
        String lastName,
        PhoneNumber phoneNumber,
        Dni dni
){ }
