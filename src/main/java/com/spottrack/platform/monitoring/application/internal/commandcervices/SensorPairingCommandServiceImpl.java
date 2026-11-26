package com.spottrack.platform.monitoring.application.internal.commandcervices;

import com.spottrack.platform.monitoring.application.commandServices.SensorPairingCommandService;
import com.spottrack.platform.monitoring.application.ports.BrokerCredentialProvisioner;
import com.spottrack.platform.monitoring.application.ports.BrokerProvisioningException;
import com.spottrack.platform.monitoring.application.ports.PairingSecretHasher;
import com.spottrack.platform.monitoring.application.ports.PayloadProfileCatalog;
import com.spottrack.platform.monitoring.application.ports.SensorPairingSettings;
import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.domain.model.commands.ExpireDueSensorPairingsCommand;
import com.spottrack.platform.monitoring.domain.model.commands.IssueSensorPairingCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MarkSensorPairingUsedCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.IssuedSensorPairing;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingQrPayload;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.monitoring.domain.repositories.SensorPairingRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;

@Service
public class SensorPairingCommandServiceImpl implements SensorPairingCommandService {
    private static final Logger log = LoggerFactory.getLogger(SensorPairingCommandServiceImpl.class);
    private static final int SECRET_BYTES = 24;

    private final MotionSensorRepository motionSensorRepository;
    private final SensorPairingRepository sensorPairingRepository;
    private final BrokerCredentialProvisioner brokerCredentialProvisioner;
    private final PairingSecretHasher pairingSecretHasher;
    private final SensorPairingSettings settings;
    private final PayloadProfileCatalog payloadProfileCatalog;
    private final TransactionOperations transactionOperations;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public SensorPairingCommandServiceImpl(MotionSensorRepository motionSensorRepository,
                                           SensorPairingRepository sensorPairingRepository,
                                           BrokerCredentialProvisioner brokerCredentialProvisioner,
                                           PairingSecretHasher pairingSecretHasher,
                                           SensorPairingSettings settings,
                                           PayloadProfileCatalog payloadProfileCatalog,
                                           TransactionOperations transactionOperations, Clock clock) {
        this.motionSensorRepository = motionSensorRepository;
        this.sensorPairingRepository = sensorPairingRepository;
        this.brokerCredentialProvisioner = brokerCredentialProvisioner;
        this.pairingSecretHasher = pairingSecretHasher;
        this.settings = settings;
        this.payloadProfileCatalog = payloadProfileCatalog;
        this.transactionOperations = transactionOperations;
        this.clock = clock;
    }

    @Override
    public Result<IssuedSensorPairing, ApplicationError> handle(IssueSensorPairingCommand command) {
        try {
            var sensorId = new MotionSensorId(command.motionSensorId());
            var found = motionSensorRepository.findByMotionSensorId(sensorId);
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("MotionSensor", sensorId.uuid()));
            }
            var sensor = found.get();
            // Checked before touching the brokers: an unknown profile must leave no account behind.
            var requestedProfile = command.payloadProfile();
            var assignsProfile = requestedProfile != null && !requestedProfile.isBlank();
            if (assignsProfile && !payloadProfileCatalog.isSupported(requestedProfile)) {
                return Result.failure(ApplicationError.validationError(
                        "payloadProfile", "Unknown sensor payload profile '%s'".formatted(requestedProfile)));
            }
            try {
                settings.requireConfigured();
            } catch (IllegalStateException e) {
                return Result.failure(ApplicationError.businessRuleViolation("Sensor pairing is configured", e.getMessage()));
            }

            var secret = generateSecret();
            var pairing = SensorPairing.issue(sensorId, pairingSecretHasher.hash(secret), LocalDateTime.now(clock), settings.ttl());

            try {
                brokerCredentialProvisioner.provision(sensorId.uuid(), secret);
            } catch (BrokerProvisioningException e) {
                log.warn("Could not provision the broker credential of device_id={}: {}", sensorId.uuid(), e.getMessage());
                return Result.failure(ApplicationError.unexpected("Broker credential provisioning", e.getMessage()));
            }

            var profileChanges = assignsProfile && !Objects.equals(requestedProfile, sensor.getPayloadProfile());
            try {
                transactionOperations.executeWithoutResult(status -> {
                    if (profileChanges) {
                        sensor.assignPayloadProfile(requestedProfile);
                        motionSensorRepository.save(sensor);
                    }
                    // The new secret already replaced the previous credential on the brokers.
                    for (var previous : sensorPairingRepository.findActiveByMotionSensorId(sensorId)) {
                        previous.revoke();
                        sensorPairingRepository.save(previous);
                    }
                    sensorPairingRepository.save(pairing);
                });
            } catch (RuntimeException e) {
                revokeQuietly(sensorId);
                throw e;
            }

            var profile = payloadProfileCatalog.effectiveProfile(assignsProfile ? requestedProfile : sensor.getPayloadProfile());
            log.info("Issued a sensor pairing for device_id={} with profile={} expiring at {}",
                    sensorId.uuid(), profile, pairing.getExpiresAt());
            var expiresAt = pairing.getExpiresAt();
            var payload = new SensorPairingQrPayload(SensorPairingQrPayload.VERSION, sensorId.uuid(), secret,
                    settings.host(), settings.port(), settings.normalizedCaSha256(),
                    expiresAt.atZone(clock.getZone()).toEpochSecond(), profile);
            return Result.success(new IssuedSensorPairing(payload, expiresAt));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("MotionSensor", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Sensor pairing issuance", e.getMessage()));
        }
    }

    @Override
    public Result<SensorPairing, ApplicationError> handle(MarkSensorPairingUsedCommand command) {
        try {
            var pending = sensorPairingRepository.findPendingByMotionSensorId(command.motionSensorId());
            if (pending.isEmpty()) {
                return Result.failure(ApplicationError.notFound("SensorPairing", command.motionSensorId().uuid()));
            }
            var now = LocalDateTime.now(clock);
            var usable = pending.stream().filter(pairing -> !pairing.isExpiredAt(now)).findFirst();
            if (usable.isEmpty()) {
                return Result.failure(ApplicationError.businessRuleViolation(
                        "Sensor pairing is not expired", "The pairing expired before the first heartbeat"));
            }
            var pairing = usable.get();
            pairing.markUsed(now);
            var saved = sensorPairingRepository.save(pairing);
            log.info("Sensor pairing used by device_id={}", command.motionSensorId().uuid());
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Sensor pairing usage", e.getMessage()));
        }
    }

    @Override
    public Result<Integer, ApplicationError> handle(ExpireDueSensorPairingsCommand command) {
        try {
            var now = LocalDateTime.now(clock);
            int expired = 0;
            for (var pairing : sensorPairingRepository.findPendingExpiredBefore(now)) {
                if (!pairing.isPending() || !pairing.isExpiredAt(now)) {
                    continue;
                }
                var deviceId = pairing.getMotionSensorId().uuid();
                try {
                    brokerCredentialProvisioner.revoke(deviceId);
                    pairing.expire(now);
                    sensorPairingRepository.save(pairing);
                    expired++;
                } catch (RuntimeException e) {
                    log.warn("Could not expire the sensor pairing of device_id={}, it will be retried: {}", deviceId, e.getMessage());
                }
            }
            return Result.success(expired);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Sensor pairing expiration", e.getMessage()));
        }
    }

    private String generateSecret() {
        var bytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void revokeQuietly(MotionSensorId sensorId) {
        try {
            brokerCredentialProvisioner.revoke(sensorId.uuid());
        } catch (RuntimeException e) {
            log.warn("Could not roll back the broker credential of device_id={}: {}", sensorId.uuid(), e.getMessage());
        }
    }
}
