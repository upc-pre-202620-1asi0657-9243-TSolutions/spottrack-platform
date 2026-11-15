package com.spottrack.platform.monitoring.domain.model.valueobjects;

import java.time.LocalDateTime;

public record IssuedSensorPairing(SensorPairingQrPayload qrPayload, LocalDateTime expiresAt) {
}
