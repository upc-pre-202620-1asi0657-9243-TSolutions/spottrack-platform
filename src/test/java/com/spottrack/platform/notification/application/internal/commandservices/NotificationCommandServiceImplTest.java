package com.spottrack.platform.notification.application.internal.commandservices;

import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;
import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.domain.model.commands.MarkNotificationAsReadCommand;
import com.spottrack.platform.notification.domain.model.commands.NotifyAvailabilityChangeCommand;
import com.spottrack.platform.notification.domain.model.commands.RegisterAffectedClientCommand;
import com.spottrack.platform.notification.domain.model.commands.UnwatchEquipmentCommand;
import com.spottrack.platform.notification.domain.model.commands.WatchEquipmentCommand;
import com.spottrack.platform.notification.domain.model.events.ClientNotificationCreatedEvent;
import com.spottrack.platform.notification.domain.model.valueobjects.AvailabilityChange;
import com.spottrack.platform.notification.domain.model.valueobjects.NotificationType;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchOrigin;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchStatus;
import com.spottrack.platform.notification.domain.repositories.ClientNotificationRepository;
import com.spottrack.platform.notification.domain.repositories.EquipmentWatchRepository;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationCommandServiceImplTest {

    private static final String EQUIPMENT = "eq-1";
    private static final String GYM = "gym-1";
    private static final Long ANA = 1L;
    private static final Long BRUNO = 2L;
    private static final Instant NOW = Instant.parse("2026-11-20T15:00:00Z");

    private InMemoryWatchRepository watches;
    private InMemoryNotificationRepository notifications;
    private GymContextFacade gym;
    private ProfilesContextFacade profiles;
    private ReservationContextFacade reservations;
    private ApplicationEventPublisher events;
    private Clock clock;
    private NotificationCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        watches = new InMemoryWatchRepository();
        notifications = new InMemoryNotificationRepository();
        gym = mock(GymContextFacade.class);
        profiles = mock(ProfilesContextFacade.class);
        reservations = mock(ReservationContextFacade.class);
        events = mock(ApplicationEventPublisher.class);
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = newService(clock);

        equipmentIs(EquipmentStatus.OCCUPIED);
        when(gym.resolveGymIdForEquipment(EQUIPMENT)).thenReturn(Optional.of(GYM));
        when(profiles.fetchActiveGymIdByClientId(any())).thenReturn(GYM);
        when(reservations.fetchActiveReservationClientIdByEquipmentId(EQUIPMENT)).thenReturn(Optional.empty());
    }

    // --- Criterion 1: availability (US12) ---

    @Test
    void clientWithTheBellOnIsNotifiedWhenTheMachineIsReleased() {
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));

        var created = service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.RELEASED));

        assertThat(created).singleElement().satisfies(n -> {
            assertThat(n.getClientId()).isEqualTo(ANA);
            assertThat(n.getType()).isEqualTo(NotificationType.EQUIPMENT_AVAILABLE);
            assertThat(n.getMessage()).isEqualTo("Leg Press is free now.");
        });
        assertThat(watches.all()).singleElement().extracting(EquipmentWatch::getStatus).isEqualTo(WatchStatus.NOTIFIED);
        verify(events).publishEvent(any(ClientNotificationCreatedEvent.class));
    }

    @Test
    void anomalyReportersAreNotNotifiedWhenTheMachineIsMerelyReleased() {
        service.handle(new RegisterAffectedClientCommand(BRUNO, EQUIPMENT));

        var created = service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.RELEASED));

        assertThat(created).isEmpty();
        assertThat(watches.all()).singleElement().extracting(EquipmentWatch::getStatus).isEqualTo(WatchStatus.ACTIVE);
        verify(events, never()).publishEvent(any());
    }

    @Test
    void clientsWhoTurnedTheBellOffAreNotNotified() {
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));
        service.handle(new UnwatchEquipmentCommand(ANA, EQUIPMENT));

        assertThat(service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.RELEASED))).isEmpty();
    }

    // --- Criterion 2: restoration (US23) ---

    @Test
    void affectedClientsAreNotifiedOnceWhenTheMachineIsBackInService() {
        equipmentIs(EquipmentStatus.MAINTENANCE);
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));
        service.handle(new RegisterAffectedClientCommand(ANA, EQUIPMENT));
        service.handle(new RegisterAffectedClientCommand(BRUNO, EQUIPMENT));

        var created = service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.RESTORED));

        assertThat(created).extracting(ClientNotification::getClientId).containsExactlyInAnyOrder(ANA, BRUNO);
        assertThat(created).allSatisfy(n -> {
            assertThat(n.getType()).isEqualTo(NotificationType.EQUIPMENT_RESTORED);
            assertThat(n.getMessage()).isEqualTo("Leg Press is back in service.");
        });
        assertThat(watches.all()).filteredOn(w -> w.getOrigin() == WatchOrigin.ANOMALY_REPORT)
                .allSatisfy(w -> assertThat(w.getStatus()).isEqualTo(WatchStatus.CLOSED));
        assertThat(watches.all()).filteredOn(EquipmentWatch::isRequestedByClient)
                .singleElement().extracting(EquipmentWatch::getStatus).isEqualTo(WatchStatus.NOTIFIED);
    }

    // --- Automatic alert discard (US12, scenario 2) ---

    @Test
    void notifiedClientIsToldWhenSomeoneElseTakesTheMachineFirst() {
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));
        service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.RELEASED));
        when(reservations.fetchActiveReservationClientIdByEquipmentId(EQUIPMENT)).thenReturn(Optional.of(BRUNO));

        var created = service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.TAKEN));

        assertThat(created).singleElement().satisfies(n -> {
            assertThat(n.getClientId()).isEqualTo(ANA);
            assertThat(n.getType()).isEqualTo(NotificationType.EQUIPMENT_OCCUPIED_AGAIN);
        });
        assertThat(watches.all()).singleElement().extracting(EquipmentWatch::getStatus).isEqualTo(WatchStatus.CLOSED);
    }

    @Test
    void notifiedClientWhoReservesTheMachineThemselvesIsNotToldItIsOccupied() {
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));
        service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.RELEASED));
        when(reservations.fetchActiveReservationClientIdByEquipmentId(EQUIPMENT)).thenReturn(Optional.of(ANA));

        assertThat(service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.TAKEN))).isEmpty();
        assertThat(watches.all()).singleElement().extracting(EquipmentWatch::getStatus).isEqualTo(WatchStatus.CLOSED);
    }

    @Test
    void occupationLongAfterTheNoticeOnlyStopsTheTracking() {
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));
        service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.RELEASED));
        service = newService(Clock.offset(clock, NotificationCommandServiceImpl.ARRIVAL_WINDOW.plus(Duration.ofMinutes(1))));

        assertThat(service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.TAKEN))).isEmpty();
        assertThat(watches.all()).singleElement().extracting(EquipmentWatch::getStatus).isEqualTo(WatchStatus.CLOSED);
    }

    @Test
    void decommissioningClosesEveryWatch() {
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));
        service.handle(new RegisterAffectedClientCommand(BRUNO, EQUIPMENT));

        assertThat(service.handle(new NotifyAvailabilityChangeCommand(EQUIPMENT, AvailabilityChange.RETIRED))).isEmpty();
        assertThat(watches.all()).allSatisfy(w -> assertThat(w.getStatus()).isEqualTo(WatchStatus.CLOSED));
    }

    // --- Bell rules ---

    @Test
    void anAvailableMachineCannotBeWatched() {
        equipmentIs(EquipmentStatus.AVAILABLE);

        var result = service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));

        assertThat(errorCode(result)).isEqualTo("BUSINESS_RULE_VIOLATION");
        assertThat(watches.all()).isEmpty();
    }

    @Test
    void aMachineOfAnotherGymCannotBeWatched() {
        when(profiles.fetchActiveGymIdByClientId(ANA)).thenReturn("gym-2");

        var result = service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));

        assertThat(errorCode(result)).isEqualTo("FORBIDDEN");
    }

    @Test
    void turningOnABellTwiceKeepsASingleWatch() {
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));
        service.handle(new WatchEquipmentCommand(ANA, EQUIPMENT));

        assertThat(watches.all()).hasSize(1);
    }

    // --- Inbox ---

    @Test
    void aClientCannotReadAnotherClientsNotification() {
        var notification = notifications.save(ClientNotification.create(
                ANA, EQUIPMENT, NotificationType.EQUIPMENT_AVAILABLE, "free", Date.from(NOW)));

        var result = service.handle(new MarkNotificationAsReadCommand(notification.getId(), BRUNO));

        assertThat(result).isInstanceOf(Result.Failure.class);
        assertThat(notifications.findById(notification.getId())).get().extracting(ClientNotification::isRead).isEqualTo(false);
    }

    @Test
    void markingAsReadRecordsWhen() {
        var notification = notifications.save(ClientNotification.create(
                ANA, EQUIPMENT, NotificationType.EQUIPMENT_AVAILABLE, "free", Date.from(NOW)));

        service.handle(new MarkNotificationAsReadCommand(notification.getId(), ANA));

        assertThat(notifications.findById(notification.getId())).get()
                .extracting(ClientNotification::getReadAt).isEqualTo(Date.from(NOW));
    }

    private static String errorCode(Result<?, ApplicationError> result) {
        assertThat(result).isInstanceOf(Result.Failure.class);
        return ((Result.Failure<?, ApplicationError>) result).error().code();
    }

    private NotificationCommandServiceImpl newService(Clock clock) {
        return new NotificationCommandServiceImpl(watches, notifications, gym, profiles, reservations, events, clock);
    }

    private void equipmentIs(EquipmentStatus status) {
        when(gym.findEquipmentSnapshotById(EQUIPMENT)).thenReturn(Optional.of(
                new EquipmentSnapshot(EQUIPMENT, "Leg Press", null, status, null)));
    }

    private static final class InMemoryWatchRepository implements EquipmentWatchRepository {
        private final Map<Long, EquipmentWatch> rows = new HashMap<>();
        private final AtomicLong ids = new AtomicLong();

        List<EquipmentWatch> all() {
            return rows.values().stream().sorted(Comparator.comparing(EquipmentWatch::getId)).toList();
        }

        @Override
        public Optional<EquipmentWatch> findActiveByClientIdAndEquipmentIdAndOrigin(Long clientId, String equipmentId, WatchOrigin origin) {
            return all().stream()
                    .filter(w -> w.getClientId().equals(clientId) && w.getEquipmentId().equals(equipmentId)
                            && w.getOrigin() == origin && w.isActive())
                    .findFirst();
        }

        @Override
        public List<EquipmentWatch> findAllByClientIdAndStatus(Long clientId, WatchStatus status) {
            return all().stream().filter(w -> w.getClientId().equals(clientId) && w.getStatus() == status).toList();
        }

        @Override
        public List<EquipmentWatch> findAllByEquipmentIdAndStatus(String equipmentId, WatchStatus status) {
            return all().stream().filter(w -> w.getEquipmentId().equals(equipmentId) && w.getStatus() == status).toList();
        }

        @Override
        public EquipmentWatch save(EquipmentWatch watch) {
            if (watch.getId() == null) {
                watch.setId(ids.incrementAndGet());
            }
            rows.put(watch.getId(), watch);
            return watch;
        }
    }

    private static final class InMemoryNotificationRepository implements ClientNotificationRepository {
        private final Map<Long, ClientNotification> rows = new HashMap<>();
        private final AtomicLong ids = new AtomicLong();

        @Override
        public Optional<ClientNotification> findById(Long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public List<ClientNotification> findAllByClientId(Long clientId, boolean unreadOnly) {
            var result = new ArrayList<ClientNotification>();
            rows.values().stream()
                    .filter(n -> n.getClientId().equals(clientId) && (!unreadOnly || !n.isRead()))
                    .forEach(result::add);
            return result;
        }

        @Override
        public ClientNotification save(ClientNotification notification) {
            if (notification.getId() == null) {
                notification.setId(ids.incrementAndGet());
            }
            rows.put(notification.getId(), notification);
            return notification;
        }
    }
}
