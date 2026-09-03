package com.spottrack.platform.gym.domain.model.commands;

public record RemoveDniFromWhitelistCommand(String gymId, String dni) {
}
