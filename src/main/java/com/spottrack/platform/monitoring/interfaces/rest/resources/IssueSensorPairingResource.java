package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record IssueSensorPairingResource(
        @Schema(description = "Optional payload profile to assign to the sensor while pairing it (e.g. \"compact-emulator\"). "
                + "When omitted, the sensor keeps the profile it has.",
                nullable = true, example = "compact-emulator")
        String payloadProfile
) {
}
