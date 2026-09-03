package com.spottrack.platform.profiles.domain.model.commands;

public record ChangeActiveGymCommand(Long clientId, String gymId) {
}
