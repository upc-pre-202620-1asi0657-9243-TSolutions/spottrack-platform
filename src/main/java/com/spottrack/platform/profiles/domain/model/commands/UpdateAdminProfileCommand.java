package com.spottrack.platform.profiles.domain.model.commands;

import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;
import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;

public record UpdateAdminProfileCommand(
        AdminId adminId,
        String firstName,
        String lastName,
        PhoneNumber phoneNumber,
        Dni dni
){ }
