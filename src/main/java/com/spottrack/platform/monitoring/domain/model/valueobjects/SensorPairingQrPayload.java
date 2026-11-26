package com.spottrack.platform.monitoring.domain.model.valueobjects;

/**
 * Content of the pairing QR, format v2 (see docs/sensor-qr-pairing.md).
 * {@code secret} is the MQTT password: it is shown once and never logged.
 * {@code profile} is the payload profile the device must publish its signals with.
 */
public record SensorPairingQrPayload(int v, String deviceId, String secret, String host, int port,
                                     String caSha256, long exp, String profile) {
    public static final int VERSION = 2;

    @Override
    public String toString() {
        return "SensorPairingQrPayload[v=%d, deviceId=%s, secret=****, host=%s, port=%d, caSha256=%s, exp=%d, profile=%s]"
                .formatted(v, deviceId, host, port, caSha256, exp, profile);
    }
}
