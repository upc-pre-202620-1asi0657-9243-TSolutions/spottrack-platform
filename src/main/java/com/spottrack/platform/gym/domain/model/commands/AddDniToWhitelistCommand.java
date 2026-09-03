package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.Dni;

public record AddDniToWhitelistCommand(String gymId, Dni dni) {
}
