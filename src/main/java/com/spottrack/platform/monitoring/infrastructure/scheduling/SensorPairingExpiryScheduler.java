package com.spottrack.platform.monitoring.infrastructure.scheduling;

import com.spottrack.platform.monitoring.application.commandServices.SensorPairingCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.ExpireDueSensorPairingsCommand;
import com.spottrack.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Expires the QR pairings that were never scanned within {@code pairing.ttl}. */
@Service
public class SensorPairingExpiryScheduler {
    private static final Logger log = LoggerFactory.getLogger(SensorPairingExpiryScheduler.class);

    private final SensorPairingCommandService sensorPairingCommandService;

    public SensorPairingExpiryScheduler(SensorPairingCommandService sensorPairingCommandService) {
        this.sensorPairingCommandService = sensorPairingCommandService;
    }

    @Scheduled(fixedRateString = "${pairing.expiry-check-interval-ms:60000}")
    public void expireUnusedPairings() {
        var result = sensorPairingCommandService.handle(new ExpireDueSensorPairingsCommand());
        if (result instanceof Result.Success<Integer, ?> success && success.value() > 0) {
            log.info("Expired {} unused sensor pairing(s)", success.value());
        } else if (result instanceof Result.Failure<?, ?> failure) {
            log.warn("Sensor pairing expiration failed: {}", failure.error());
        }
    }
}
