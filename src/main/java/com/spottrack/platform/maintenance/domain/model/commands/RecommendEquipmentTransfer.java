package com.spottrack.platform.maintenance.domain.model.commands;

public record RecommendEquipmentTransfer(
        String equipmentId,
        String reason
) {
    public RecommendEquipmentTransfer {
        if (equipmentId == null || equipmentId.isBlank())
            throw new IllegalArgumentException("maintenance.command.recommendTransfer.equipmentId.notBlank");
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("maintenance.command.recommendTransfer.reason.notBlank");
    }
}
