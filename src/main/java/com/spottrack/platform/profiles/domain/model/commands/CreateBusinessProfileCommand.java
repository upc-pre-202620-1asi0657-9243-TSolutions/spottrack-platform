package com.spottrack.platform.profiles.domain.model.commands;

import com.spottrack.platform.profiles.domain.model.valueobjects.BusinessInfo;

public record CreateBusinessProfileCommand(
        Long userId,
        BusinessInfo businessInfo
) {
    public CreateBusinessProfileCommand {
        if (userId == null) throw new IllegalArgumentException("userId must not be null");
        if (businessInfo == null) throw new IllegalArgumentException("businessInfo must not be null");
    }
}
