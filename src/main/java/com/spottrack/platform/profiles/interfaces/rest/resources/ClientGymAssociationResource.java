package com.spottrack.platform.profiles.interfaces.rest.resources;

public record ClientGymAssociationResource(Long clientId, String gymId, boolean active) {
}
