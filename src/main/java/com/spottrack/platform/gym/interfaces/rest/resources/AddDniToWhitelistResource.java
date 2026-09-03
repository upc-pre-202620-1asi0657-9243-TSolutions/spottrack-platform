package com.spottrack.platform.gym.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record AddDniToWhitelistResource(@NotBlank String dni) {
}
