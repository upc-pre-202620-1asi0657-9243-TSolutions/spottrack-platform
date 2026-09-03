package com.spottrack.platform.profiles.domain.model.commands;

public record AssociateClientWithGymCommand(Long clientId, String gymId) {
}
