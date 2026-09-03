package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceResource(
        @Schema(description = "Motion sensor id, used by the IoT simulator as its device identifier")
        String deviceId
) {
}
