package com.spottrack.platform.notification.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record WatchEquipmentResource(
        @NotBlank
        @Schema(description = "Equipment to be notified about once it is available", requiredMode = Schema.RequiredMode.REQUIRED)
        String equipmentId
) {
}
