package com.spottrack.platform.gym.domain.model.commands;

public record AddBranchCommand(String gymId,String name, String address) {
        public AddBranchCommand {
            if (gymId == null || gymId.isBlank()) throw new IllegalArgumentException("gymId must not be null or blank");
            if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be null or blank");
            if (address == null || address.isBlank()) throw new IllegalArgumentException("address must not be null or blank");
        }
}
