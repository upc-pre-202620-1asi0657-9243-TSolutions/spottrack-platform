package com.spottrack.platform.maintenance.domain.model.commands;

public record CreateTechnician(String name) {
    public CreateTechnician {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("maintenance.command.createTechnician.name.notBlank");
        }
    }
}
