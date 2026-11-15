package com.spottrack.platform.monitoring.infrastructure.scheduling;

import com.spottrack.platform.monitoring.application.commandServices.SensorPairingCommandService;
import com.spottrack.platform.monitoring.application.internal.commandcervices.SensorPairingCommandServiceImpl;
import com.spottrack.platform.monitoring.application.ports.BrokerCredentialProvisioner;
import com.spottrack.platform.monitoring.application.ports.BrokerProvisioningException;
import com.spottrack.platform.monitoring.application.ports.PairingSecretHasher;
import com.spottrack.platform.monitoring.application.ports.SensorPairingSettings;
import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.domain.model.commands.ExpireDueSensorPairingsCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.monitoring.testsupport.InMemorySensorPairingRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SensorPairingExpirySchedulerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 12, 0);
    private static final Duration TTL = Duration.ofMinutes(15);

    @Test
    void theTickRunsTheExpirationCommand() {
        var commandService = mock(SensorPairingCommandService.class);
        when(commandService.handle(any(ExpireDueSensorPairingsCommand.class))).thenReturn(Result.success(2));

        new SensorPairingExpiryScheduler(commandService).expireUnusedPairings();

        verify(commandService).handle(new ExpireDueSensorPairingsCommand());
    }

    @Test
    void aFailureOfTheCommandDoesNotPropagateOutOfTheTick() {
        var commandService = mock(SensorPairingCommandService.class);
        when(commandService.handle(any(ExpireDueSensorPairingsCommand.class)))
                .thenReturn(Result.failure(ApplicationError.unexpected("test", "boom")));

        assertDoesNotThrow(() -> new SensorPairingExpiryScheduler(commandService).expireUnusedPairings());
    }

    @Test
    void theIntervalIsConfigurableByProperty() throws NoSuchMethodException {
        var scheduled = SensorPairingExpiryScheduler.class.getMethod("expireUnusedPairings").getAnnotation(Scheduled.class);

        assertEquals("${pairing.expiry-check-interval-ms:60000}", scheduled.fixedRateString());
    }

    @Nested
    class AgainstTheRealService {
        private InMemorySensorPairingRepository pairings;
        private BrokerCredentialProvisioner provisioner;
        private SensorPairingExpiryScheduler scheduler;

        @BeforeEach
        void setUp() {
            pairings = new InMemorySensorPairingRepository();
            provisioner = mock(BrokerCredentialProvisioner.class);
            var service = new SensorPairingCommandServiceImpl(mock(MotionSensorRepository.class), pairings, provisioner,
                    mock(PairingSecretHasher.class), new SensorPairingSettings(TTL, "h:8883", "a".repeat(64)),
                    Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
            scheduler = new SensorPairingExpiryScheduler(service);
        }

        private SensorPairing pending(String device, LocalDateTime issuedAt) {
            return pairings.save(SensorPairing.issue(new MotionSensorId(device), "hash", issuedAt, TTL));
        }

        @Test
        void expiredPendingPairingsAreRevokedAtTheProvisionerAndMarkedExpired() {
            var stale = pending("dev-1", NOW.minusHours(1));

            scheduler.expireUnusedPairings();

            verify(provisioner).revoke("dev-1");
            assertEquals(SensorPairingStatus.EXPIRED, stale.getStatus());
        }

        @Test
        void aUsedPairingIsNeverRevokedNorExpiredEvenLongAfterItsTtl() {
            var used = pending("dev-2", NOW.minusDays(3));
            used.markUsed(NOW.minusDays(3).plusMinutes(1));

            scheduler.expireUnusedPairings();

            verifyNoInteractions(provisioner);
            assertEquals(SensorPairingStatus.USED, used.getStatus());
        }

        @Test
        void aPendingPairingStillInsideItsTtlIsNotTouched() {
            var fresh = pending("dev-3", NOW.minusMinutes(5));

            scheduler.expireUnusedPairings();

            verify(provisioner, never()).revoke(anyString());
            assertEquals(SensorPairingStatus.PENDING, fresh.getStatus());
        }

        @Test
        void aFailedRevokeIsRetriedOnTheNextTick() {
            var stale = pending("dev-4", NOW.minusHours(1));
            doThrow(new BrokerProvisioningException("broker down")).doNothing().when(provisioner).revoke("dev-4");

            scheduler.expireUnusedPairings();
            assertEquals(SensorPairingStatus.PENDING, stale.getStatus());

            scheduler.expireUnusedPairings();
            assertEquals(SensorPairingStatus.EXPIRED, stale.getStatus());
        }
    }
}
