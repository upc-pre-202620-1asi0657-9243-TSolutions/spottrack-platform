package com.spottrack.platform.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource for recommending an equipment transfer")
public record DecommissionEquipmentResource(
        @Schema(description = "Why the equipment should be transferred or decommissioned", example = "Recurring motor failure after three repairs")
        String reason
) {}
