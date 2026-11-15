package com.spottrack.platform.monitoring.domain.model.valueobjects;

/**
 * Content of the pairing QR, format v1 (see docs/sensor-qr-pairing.md).
 * {@code secret} is the MQTT password: it is shown once and never logged.
 */
public record SensorPairingQrPayload(int v, String deviceId, String secret, String host, int port,
                                     String caSha256, long exp) {
    public static final int VERSION = 1;

    @Override
    public String toString() {
        return "SensorPairingQrPayload[v=%d, deviceId=%s, secret=****, host=%s, port=%d, caSha256=%s, exp=%d]"
                .formatted(v, deviceId, host, port, caSha256, exp);
    }
}
