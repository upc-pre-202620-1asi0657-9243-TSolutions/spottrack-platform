package com.spottrack.platform.maintenance.domain.model.commands;

public record CreateTechnician(String name, Long userId, Long adminUserId) {
    public CreateTechnician {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Technician name must not be blank");
        }
    }

    public CreateTechnician(String name) {
        this(name, null, null);
    }
}
