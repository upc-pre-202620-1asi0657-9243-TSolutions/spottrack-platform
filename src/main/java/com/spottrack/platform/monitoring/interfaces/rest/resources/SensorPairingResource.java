package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record SensorPairingResource(
        @Schema(description = "Motion sensor the pairing was issued for")
        String motionSensorId,
        @Schema(description = "Until when the QR can be used if it has not been scanned (first heartbeat)")
        LocalDateTime expiresAt,
        @Schema(description = "Structured content of the QR")
        SensorPairingQrPayloadResource payload,
        @Schema(description = "Exact JSON encoded in the QR", example = "{\"v\":1,\"deviceId\":\"...\"}")
        String payloadJson,
        @Schema(description = "PNG image of the QR, Base64 encoded")
        String qrPngBase64
) {
    @Override
    public String toString() {
        return "SensorPairingResource[motionSensorId=%s, expiresAt=%s, payload=%s, payloadJson=****, qrPngBase64=****]"
                .formatted(motionSensorId, expiresAt, payload);
    }
}
