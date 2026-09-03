package com.spottrack.platform.profiles.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record ChangeActiveGymResource(@NotBlank String gymId) {
}
