package com.spottrack.platform.gym.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record AddBranchResource(@NotBlank String name, @NotBlank String address) {
}
