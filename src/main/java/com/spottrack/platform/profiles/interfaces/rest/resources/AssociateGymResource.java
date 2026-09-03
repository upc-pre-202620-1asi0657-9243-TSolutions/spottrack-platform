package com.spottrack.platform.profiles.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record AssociateGymResource(@NotBlank String gymId) {
}
