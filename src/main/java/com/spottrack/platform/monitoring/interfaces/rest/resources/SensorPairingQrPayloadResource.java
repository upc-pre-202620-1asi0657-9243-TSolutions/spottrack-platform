package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Content of the pairing QR, format v2. The field order is part of the format.")
public record SensorPairingQrPayloadResource(
        @Schema(description = "Format version; a device must reject versions it does not know", example = "2")
        int v,
        @Schema(description = "Motion sensor id: the MQTT user and part of the topics", example = "066dd222-31ed-496a-bddf-e4711b0ee957")
        String deviceId,
        @Schema(description = "MQTT password (24 random bytes, Base64 URL without padding). Returned only once")
        String secret,
        @Schema(description = "Host of the MQTT brokers' load balancer", example = "mqtt.example.com")
        String host,
        @Schema(description = "TLS port of the brokers", example = "8883")
        int port,
        @Schema(description = "SHA-256 of the broker CA certificate (DER), lowercase hex",
                example = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        String caSha256,
        @Schema(description = "Epoch seconds (UTC) until which the QR is valid if it has not been scanned", example = "1790000000")
        long exp,
        @Schema(description = "Payload profile the device must publish its signals with", example = "default")
        String profile
) {
    @Override
    public String toString() {
        return "SensorPairingQrPayloadResource[v=%d, deviceId=%s, secret=****, host=%s, port=%d, caSha256=%s, exp=%d, profile=%s]"
                .formatted(v, deviceId, host, port, caSha256, exp, profile);
    }
}
