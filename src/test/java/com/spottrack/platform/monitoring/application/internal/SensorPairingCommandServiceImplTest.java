package com.spottrack.platform.monitoring.application.internal;

import com.spottrack.platform.monitoring.application.internal.commandcervices.SensorPairingCommandServiceImpl;
import com.spottrack.platform.monitoring.application.ports.BrokerCredentialProvisioner;
import com.spottrack.platform.monitoring.application.ports.BrokerProvisioningException;
import com.spottrack.platform.monitoring.application.ports.PairingSecretHasher;
import com.spottrack.platform.monitoring.application.ports.SensorPairingSettings;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.domain.model.commands.ExpireDueSensorPairingsCommand;
import com.spottrack.platform.monitoring.domain.model.commands.IssueSensorPairingCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MarkSensorPairingUsedCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.IssuedSensorPairing;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.monitoring.domain.repositories.SensorPairingRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SensorPairingCommandServiceImplTest {

    private static final String SENSOR_UUID = "066dd222-31ed-496a-bddf-e4711b0ee957";
    private static final MotionSensorId SENSOR = new MotionSensorId(SENSOR_UUID);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 12, 0);
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final String CA = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

    private MotionSensorRepository sensors;
    private SensorPairingRepository pairings;
    private BrokerCredentialProvisioner provisioner;
    private PairingSecretHasher hasher;
    private Clock clock;
    private SensorPairingCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        sensors = mock(MotionSensorRepository.class);
        pairings = mock(SensorPairingRepository.class);
        provisioner = mock(BrokerCredentialProvisioner.class);
        hasher = mock(PairingSecretHasher.class);
        when(hasher.hash(anyString())).thenAnswer(invocation -> "hashed:" + invocation.getArgument(0));
        when(pairings.save(any(SensorPairing.class))).thenAnswer(invocation -> invocation.getArgument(0));
        clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = serviceWith(new SensorPairingSettings(TTL, "34.123.45.67:8883", CA));
    }

    private SensorPairingCommandServiceImpl serviceWith(SensorPairingSettings settings) {
        return new SensorPairingCommandServiceImpl(sensors, pairings, provisioner, hasher, settings, clock);
    }

    private void givenSensor() {
        when(sensors.findByMotionSensorId(SENSOR)).thenReturn(Optional.of(
                new MotionSensor(1L, SENSOR, new EquipmentId("eq-1"), NOW, false, NOW, null, null)));
    }

    private IssuedSensorPairing issue() {
        var result = service.handle(new IssueSensorPairingCommand(SENSOR_UUID));
        return ((Result.Success<IssuedSensorPairing, ApplicationError>) assertInstanceOf(Result.Success.class, result)).value();
    }

    private static ApplicationError errorOf(Result<?, ApplicationError> result) {
        return ((Result.Failure<?, ApplicationError>) assertInstanceOf(Result.Failure.class, result)).error();
    }

    // --- issuance ---

    @Test
    void issuingReturnsTheQrPayloadForTheDevice() {
        givenSensor();

        var issued = issue();

        var payload = issued.qrPayload();
        assertEquals(1, payload.v());
        assertEquals(SENSOR_UUID, payload.deviceId());
        assertEquals("34.123.45.67", payload.host());
        assertEquals(8883, payload.port());
        assertEquals(CA, payload.caSha256());
        assertEquals(NOW.plus(TTL), issued.expiresAt());
        assertEquals(NOW.plus(TTL).toEpochSecond(ZoneOffset.UTC), payload.exp());
    }

    @Test
    void secretHasAtLeast24BytesOfEntropyInBase64Url() {
        givenSensor();

        var secret = issue().qrPayload().secret();

        assertEquals(32, secret.length());
        assertTrue(secret.matches("[A-Za-z0-9_-]{32}"));
    }

    @Test
    void everyIssuanceGeneratesADifferentSecret() {
        givenSensor();

        assertNotEquals(issue().qrPayload().secret(), issue().qrPayload().secret());
    }

    @Test
    void provisionsTheDeviceWithTheSameSecretThatGoesInTheQr() {
        givenSensor();

        var secret = issue().qrPayload().secret();

        verify(provisioner).provision(SENSOR_UUID, secret);
    }

    @Test
    void persistsOnlyTheHashOfTheSecretAsPending() {
        givenSensor();

        var secret = issue().qrPayload().secret();

        var captor = ArgumentCaptor.forClass(SensorPairing.class);
        verify(pairings).save(captor.capture());
        var saved = captor.getValue();
        assertEquals("hashed:" + secret, saved.getSecretHash());
        assertNotEquals(secret, saved.getSecretHash());
        assertEquals(SensorPairingStatus.PENDING, saved.getStatus());
        assertEquals(NOW.plus(TTL), saved.getExpiresAt());
        assertEquals(SENSOR, saved.getMotionSensorId());
    }

    @Test
    void provisionsBeforePersistingSoAFailureLeavesNothingBehind() {
        givenSensor();

        issue();

        InOrder order = inOrder(provisioner, pairings);
        order.verify(provisioner).provision(anyString(), anyString());
        order.verify(pairings).save(any(SensorPairing.class));
    }

    @Test
    void unknownSensorIsNotFoundAndNothingIsProvisioned() {
        when(sensors.findByMotionSensorId(SENSOR)).thenReturn(Optional.empty());

        var result = service.handle(new IssueSensorPairingCommand(SENSOR_UUID));

        assertEquals(ApplicationError.notFound("MotionSensor", SENSOR_UUID), errorOf(result));
        verify(provisioner, never()).provision(anyString(), anyString());
        verify(pairings, never()).save(any());
    }

    @Test
    void blankSensorIdIsAValidationError() {
        var result = service.handle(new IssueSensorPairingCommand(" "));

        assertEquals("VALIDATION_ERROR", errorOf(result).code());
        verify(provisioner, never()).provision(anyString(), anyString());
    }

    @Test
    void provisioningFailureIsAnUnexpectedErrorAndPersistsNothing() {
        givenSensor();
        doThrow(new BrokerProvisioningException("broker 2 unreachable")).when(provisioner).provision(anyString(), anyString());

        var result = service.handle(new IssueSensorPairingCommand(SENSOR_UUID));

        assertEquals("UNEXPECTED_ERROR", errorOf(result).code());
        verify(pairings, never()).save(any());
        verify(pairings, never()).findActiveByMotionSensorId(any());
    }

    @Test
    void regeneratingRevokesThePreviousPendingAndUsedPairings() {
        givenSensor();
        var pending = SensorPairing.issue(SENSOR, "old-1", NOW.minusMinutes(5), TTL);
        var used = SensorPairing.issue(SENSOR, "old-2", NOW.minusDays(1), TTL);
        used.markUsed(NOW.minusDays(1).plusMinutes(1));
        when(pairings.findActiveByMotionSensorId(SENSOR)).thenReturn(List.of(pending, used));

        issue();

        assertEquals(SensorPairingStatus.REVOKED, pending.getStatus());
        assertEquals(SensorPairingStatus.REVOKED, used.getStatus());
        verify(pairings).save(pending);
        verify(pairings).save(used);
    }

    @Test
    void regeneratingDoesNotRevokeTheNewCredentialOnTheBrokers() {
        givenSensor();
        when(pairings.findActiveByMotionSensorId(SENSOR)).thenReturn(
                List.of(SensorPairing.issue(SENSOR, "old", NOW.minusMinutes(5), TTL)));

        issue();

        verify(provisioner, never()).revoke(anyString());
    }

    @Test
    void persistenceFailureRollsBackTheBrokerCredential() {
        givenSensor();
        when(pairings.save(any(SensorPairing.class))).thenThrow(new IllegalStateException("db down"));

        var result = service.handle(new IssueSensorPairingCommand(SENSOR_UUID));

        assertEquals("UNEXPECTED_ERROR", errorOf(result).code());
        verify(provisioner).revoke(SENSOR_UUID);
    }

    @Test
    void emptyProdConfigurationIsABusinessRuleViolationWithAClearMessageAndNeverTouchesTheBrokers() {
        givenSensor();
        service = serviceWith(new SensorPairingSettings(TTL, "", ""));

        var error = errorOf(service.handle(new IssueSensorPairingCommand(SENSOR_UUID)));

        assertEquals("BUSINESS_RULE_VIOLATION", error.code());
        assertTrue(error.details().contains("mqtt.public-endpoint is not configured"), error.details());
        verifyNoInteractions(provisioner);
        verify(pairings, never()).save(any());
    }

    @Test
    void missingCaFingerprintIsReportedByName() {
        givenSensor();
        service = serviceWith(new SensorPairingSettings(TTL, "mqtt.example.com:8883", " "));

        var error = errorOf(service.handle(new IssueSensorPairingCommand(SENSOR_UUID)));

        assertEquals("BUSINESS_RULE_VIOLATION", error.code());
        assertTrue(error.details().contains("mqtt.ca-sha256 is not configured"), error.details());
        verifyNoInteractions(provisioner);
    }

    @Test
    void unknownSensorIsReportedBeforeMissingConfiguration() {
        when(sensors.findByMotionSensorId(SENSOR)).thenReturn(Optional.empty());
        service = serviceWith(new SensorPairingSettings(TTL, "", ""));

        assertEquals("MOTIONSENSOR_NOT_FOUND", errorOf(service.handle(new IssueSensorPairingCommand(SENSOR_UUID))).code());
    }

    // --- first heartbeat ---

    @Test
    void firstHeartbeatMarksThePendingPairingAsUsedAndKeepsTheCredential() {
        var pairing = SensorPairing.issue(SENSOR, "hash", NOW.minusMinutes(2), TTL);
        when(pairings.findPendingByMotionSensorId(SENSOR)).thenReturn(List.of(pairing));

        var result = service.handle(new MarkSensorPairingUsedCommand(SENSOR));

        assertTrue(result.isSuccess());
        assertEquals(SensorPairingStatus.USED, pairing.getStatus());
        assertEquals(NOW, pairing.getUsedAt());
        verify(pairings).save(pairing);
        verify(provisioner, never()).revoke(anyString());
    }

    @Test
    void heartbeatWithoutPendingPairingIsIgnored() {
        when(pairings.findPendingByMotionSensorId(SENSOR)).thenReturn(List.of());

        var result = service.handle(new MarkSensorPairingUsedCommand(SENSOR));

        assertEquals("SENSORPAIRING_NOT_FOUND", errorOf(result).code());
        verify(pairings, never()).save(any());
    }

    @Test
    void heartbeatAfterTheTtlDoesNotMarkThePairingAsUsed() {
        var pairing = SensorPairing.issue(SENSOR, "hash", NOW.minus(TTL).minusMinutes(1), TTL);
        when(pairings.findPendingByMotionSensorId(SENSOR)).thenReturn(List.of(pairing));

        var result = service.handle(new MarkSensorPairingUsedCommand(SENSOR));

        assertFalse(result.isSuccess());
        assertEquals(SensorPairingStatus.PENDING, pairing.getStatus());
        verify(pairings, never()).save(any());
    }

    // --- expiration ---

    @Test
    void expiringRevokesTheCredentialAndMarksThePairingExpired() {
        var pairing = SensorPairing.issue(SENSOR, "hash", NOW.minus(TTL).minusMinutes(1), TTL);
        when(pairings.findPendingExpiredBefore(NOW)).thenReturn(List.of(pairing));

        var result = service.handle(new ExpireDueSensorPairingsCommand());

        assertEquals(1, ((Result.Success<Integer, ApplicationError>) result).value());
        assertEquals(SensorPairingStatus.EXPIRED, pairing.getStatus());
        verify(provisioner).revoke(SENSOR_UUID);
        verify(pairings).save(pairing);
    }

    @Test
    void whenRevokeFailsThePairingStaysPendingToBeRetried() {
        var pairing = SensorPairing.issue(SENSOR, "hash", NOW.minus(TTL).minusMinutes(1), TTL);
        when(pairings.findPendingExpiredBefore(NOW)).thenReturn(List.of(pairing));
        doThrow(new BrokerProvisioningException("broker 1 down")).when(provisioner).revoke(SENSOR_UUID);

        var result = service.handle(new ExpireDueSensorPairingsCommand());

        assertEquals(0, ((Result.Success<Integer, ApplicationError>) result).value());
        assertEquals(SensorPairingStatus.PENDING, pairing.getStatus());
        verify(pairings, never()).save(any());
    }

    @Test
    void oneFailingRevokeDoesNotBlockTheOtherExpirations() {
        var other = new MotionSensorId("dev-2");
        var failing = SensorPairing.issue(SENSOR, "h1", NOW.minus(TTL).minusMinutes(1), TTL);
        var healthy = SensorPairing.issue(other, "h2", NOW.minus(TTL).minusMinutes(1), TTL);
        when(pairings.findPendingExpiredBefore(NOW)).thenReturn(List.of(failing, healthy));
        doThrow(new BrokerProvisioningException("down")).when(provisioner).revoke(SENSOR_UUID);

        var result = service.handle(new ExpireDueSensorPairingsCommand());

        assertEquals(1, ((Result.Success<Integer, ApplicationError>) result).value());
        assertEquals(SensorPairingStatus.PENDING, failing.getStatus());
        assertEquals(SensorPairingStatus.EXPIRED, healthy.getStatus());
    }

    @Test
    void aUsedPairingIsNeverRevokedNorExpiredEvenIfTheRepositoryReturnsIt() {
        var used = SensorPairing.issue(SENSOR, "hash", NOW.minus(TTL).minusDays(1), TTL);
        used.markUsed(NOW.minus(TTL).minusDays(1).plusMinutes(1));
        when(pairings.findPendingExpiredBefore(NOW)).thenReturn(List.of(used));

        var result = service.handle(new ExpireDueSensorPairingsCommand());

        assertEquals(0, ((Result.Success<Integer, ApplicationError>) result).value());
        assertEquals(SensorPairingStatus.USED, used.getStatus());
        verifyNoInteractions(provisioner);
        verify(pairings, never()).save(any());
    }

    @Test
    void aPendingPairingThatHasNotExpiredYetIsLeftAlone() {
        var fresh = SensorPairing.issue(SENSOR, "hash", NOW.minusMinutes(1), TTL);
        when(pairings.findPendingExpiredBefore(NOW)).thenReturn(List.of(fresh));

        service.handle(new ExpireDueSensorPairingsCommand());

        assertEquals(SensorPairingStatus.PENDING, fresh.getStatus());
        verifyNoInteractions(provisioner);
    }

    @Test
    void nothingDueMeansNothingExpired() {
        when(pairings.findPendingExpiredBefore(NOW)).thenReturn(List.of());

        var result = service.handle(new ExpireDueSensorPairingsCommand());

        assertEquals(0, ((Result.Success<Integer, ApplicationError>) result).value());
        verify(provisioner, never()).revoke(anyString());
    }
}
