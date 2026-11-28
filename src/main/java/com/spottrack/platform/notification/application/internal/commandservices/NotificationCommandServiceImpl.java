package com.spottrack.platform.notification.application.internal.commandservices;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.notification.application.commandservices.NotificationCommandService;
import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;
import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.domain.model.commands.MarkNotificationAsReadCommand;
import com.spottrack.platform.notification.domain.model.commands.NotifyAvailabilityChangeCommand;
import com.spottrack.platform.notification.domain.model.commands.RegisterAffectedClientCommand;
import com.spottrack.platform.notification.domain.model.commands.UnwatchEquipmentCommand;
import com.spottrack.platform.notification.domain.model.commands.WatchEquipmentCommand;
import com.spottrack.platform.notification.domain.model.events.ClientNotificationCreatedEvent;
import com.spottrack.platform.notification.domain.model.valueobjects.NotificationType;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchOrigin;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchStatus;
import com.spottrack.platform.notification.domain.repositories.ClientNotificationRepository;
import com.spottrack.platform.notification.domain.repositories.EquipmentWatchRepository;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
@Service
public class NotificationCommandServiceImpl implements NotificationCommandService {

    /**
     * How long after being told a machine is free the client is still considered on their way to it.
     * If someone else takes the machine within this window, the client is told it is occupied again.
     */
    static final Duration ARRIVAL_WINDOW = Duration.ofMinutes(15);

    /** Statuses in which an equipment cannot be used right now, so a bell makes sense. */
    private static final Set<EquipmentStatus> WATCHABLE_STATUSES = Set.of(
            EquipmentStatus.OCCUPIED, EquipmentStatus.RESERVED, EquipmentStatus.ACTIVE,
            EquipmentStatus.MAINTENANCE, EquipmentStatus.OUT_OF_SERVICE);

    private final EquipmentWatchRepository watchRepository;
    private final ClientNotificationRepository notificationRepository;
    private final GymContextFacade gymContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final ReservationContextFacade reservationContextFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public NotificationCommandServiceImpl(EquipmentWatchRepository watchRepository,
                                          ClientNotificationRepository notificationRepository,
                                          GymContextFacade gymContextFacade,
                                          ProfilesContextFacade profilesContextFacade,
                                          ReservationContextFacade reservationContextFacade,
                                          ApplicationEventPublisher eventPublisher,
                                          Clock clock) {
        this.watchRepository = watchRepository;
        this.notificationRepository = notificationRepository;
        this.gymContextFacade = gymContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.reservationContextFacade = reservationContextFacade;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    @Override
    public Result<EquipmentWatch, ApplicationError> handle(WatchEquipmentCommand command) {
        var equipment = gymContextFacade.findEquipmentById(command.equipmentId());
        if (equipment.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId()));
        }
        var equipmentGymId = gymContextFacade.resolveGymIdForEquipment(command.equipmentId());
        var clientGymId = profilesContextFacade.fetchActiveGymIdByClientId(command.clientId());
        if (equipmentGymId.isEmpty() || !equipmentGymId.get().equals(clientGymId)) {
            return Result.failure(ApplicationError.forbidden("Equipment",
                    "The equipment does not belong to the client's active gym."));
        }
        var status = equipment.get().getStatus();
        if (!WATCHABLE_STATUSES.contains(status)) {
            return Result.failure(ApplicationError.businessRuleViolation("equipment watch",
                    "Only an equipment that is in use, reserved or under maintenance can be watched; it is %s."
                            .formatted(status)));
        }

        var existing = watchRepository.findActiveByClientIdAndEquipmentIdAndOrigin(
                command.clientId(), command.equipmentId(), WatchOrigin.CLIENT_REQUEST);
        if (existing.isPresent()) {
            return Result.success(existing.get());
        }
        // A previous bell that already fired is superseded by the new one.
        watchRepository.findAllByEquipmentIdAndStatus(command.equipmentId(), WatchStatus.NOTIFIED).stream()
                .filter(watch -> watch.getClientId().equals(command.clientId()))
                .forEach(watch -> {
                    watch.close();
                    watchRepository.save(watch);
                });
        var watch = EquipmentWatch.start(command.clientId(), command.equipmentId(), WatchOrigin.CLIENT_REQUEST, now());
        return Result.success(watchRepository.save(watch));
    }

    @Transactional
    @Override
    public Result<EquipmentWatch, ApplicationError> handle(UnwatchEquipmentCommand command) {
        var existing = watchRepository.findActiveByClientIdAndEquipmentIdAndOrigin(
                command.clientId(), command.equipmentId(), WatchOrigin.CLIENT_REQUEST);
        if (existing.isEmpty()) {
            return Result.failure(ApplicationError.notFound("EquipmentWatch", command.equipmentId()));
        }
        var watch = existing.get();
        watch.close();
        return Result.success(watchRepository.save(watch));
    }

    /** REQUIRES_NEW: invoked from an AFTER_COMMIT listener, see {@link #handle(NotifyAvailabilityChangeCommand)}. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Result<EquipmentWatch, ApplicationError> handle(RegisterAffectedClientCommand command) {
        var existing = watchRepository.findActiveByClientIdAndEquipmentIdAndOrigin(
                command.clientId(), command.equipmentId(), WatchOrigin.ANOMALY_REPORT);
        if (existing.isPresent()) {
            return Result.success(existing.get());
        }
        var watch = EquipmentWatch.start(command.clientId(), command.equipmentId(), WatchOrigin.ANOMALY_REPORT, now());
        return Result.success(watchRepository.save(watch));
    }

    /**
     * REQUIRES_NEW: this command is invoked from a @TransactionalEventListener(AFTER_COMMIT) callback,
     * after the transaction that changed the equipment status has committed. Joining that completing
     * transaction would leave the writes unflushed.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public List<ClientNotification> handle(NotifyAvailabilityChangeCommand command) {
        var equipmentId = command.equipmentId();
        var created = switch (command.change()) {
            case RELEASED -> notifyWatchers(equipmentId, WatchStatus.ACTIVE, EquipmentWatch::isRequestedByClient,
                    NotificationType.EQUIPMENT_AVAILABLE, "%s is free now.");
            case RESTORED -> notifyWatchers(equipmentId, WatchStatus.ACTIVE, watch -> true,
                    NotificationType.EQUIPMENT_RESTORED, "%s is back in service.");
            case TAKEN -> closeNotifiedWatches(equipmentId, true);
            case BLOCKED -> closeNotifiedWatches(equipmentId, false);
            case RETIRED -> {
                closeAll(watchRepository.findAllByEquipmentIdAndStatus(equipmentId, WatchStatus.ACTIVE));
                closeAll(watchRepository.findAllByEquipmentIdAndStatus(equipmentId, WatchStatus.NOTIFIED));
                yield List.<ClientNotification>of();
            }
        };
        created.forEach(notification -> eventPublisher.publishEvent(new ClientNotificationCreatedEvent(
                notification.getId(), notification.getClientId(), notification.getEquipmentId(),
                notification.getType(), notification.getMessage(), notification.getCreatedAt())));
        return created;
    }

    @Transactional
    @Override
    public Result<ClientNotification, ApplicationError> handle(MarkNotificationAsReadCommand command) {
        var found = notificationRepository.findById(command.notificationId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Notification", command.notificationId().toString()));
        }
        var notification = found.get();
        if (!notification.getClientId().equals(command.clientId())) {
            return Result.failure(ApplicationError.forbidden("Notification",
                    "The notification belongs to another client."));
        }
        notification.markAsRead(now());
        return Result.success(notificationRepository.save(notification));
    }

    /**
     * Notifies each client following the equipment once, even if they follow it for several reasons.
     * Bells move to NOTIFIED so a later occupation can be reported; anomaly watches are done and close.
     */
    private List<ClientNotification> notifyWatchers(String equipmentId, WatchStatus from,
                                                    Predicate<EquipmentWatch> eligible,
                                                    NotificationType type, String messageTemplate) {
        var now = now();
        Set<Long> recipients = new LinkedHashSet<>();
        for (var watch : watchRepository.findAllByEquipmentIdAndStatus(equipmentId, from)) {
            if (!eligible.test(watch)) {
                continue;
            }
            if (watch.isRequestedByClient()) {
                watch.markAsNotified(now);
            } else {
                watch.close();
            }
            watchRepository.save(watch);
            recipients.add(watch.getClientId());
        }
        if (recipients.isEmpty()) {
            return List.of();
        }
        var message = messageTemplate.formatted(equipmentName(equipmentId));
        var created = new ArrayList<ClientNotification>();
        for (var clientId : recipients) {
            created.add(notificationRepository.save(
                    ClientNotification.create(clientId, equipmentId, type, message, now)));
        }
        log.info("Notified {} client(s) that equipment {} changed: {}", created.size(), equipmentId, type);
        return created;
    }

    /**
     * Stops tracking the clients already told the machine was free. When someone took it, those still
     * on their way (notified within {@link #ARRIVAL_WINDOW}) are told it is occupied again, unless the
     * one who took it through a reservation is that same client.
     */
    private List<ClientNotification> closeNotifiedWatches(String equipmentId, boolean taken) {
        var notified = watchRepository.findAllByEquipmentIdAndStatus(equipmentId, WatchStatus.NOTIFIED);
        if (notified.isEmpty()) {
            return List.of();
        }
        var now = now();
        Optional<Long> holder = taken
                ? reservationContextFacade.fetchActiveReservationClientIdByEquipmentId(equipmentId)
                : Optional.empty();
        Set<Long> recipients = new LinkedHashSet<>();
        for (var watch : notified) {
            watch.close();
            watchRepository.save(watch);
            if (taken && watch.wasNotifiedWithin(ARRIVAL_WINDOW, now)
                    && holder.map(id -> !id.equals(watch.getClientId())).orElse(true)) {
                recipients.add(watch.getClientId());
            }
        }
        if (recipients.isEmpty()) {
            return List.of();
        }
        var message = "%s was taken by someone else; it is occupied again.".formatted(equipmentName(equipmentId));
        var created = new ArrayList<ClientNotification>();
        for (var clientId : recipients) {
            created.add(notificationRepository.save(ClientNotification.create(
                    clientId, equipmentId, NotificationType.EQUIPMENT_OCCUPIED_AGAIN, message, now)));
        }
        return created;
    }

    private void closeAll(List<EquipmentWatch> watches) {
        watches.forEach(watch -> {
            watch.close();
            watchRepository.save(watch);
        });
    }

    private String equipmentName(String equipmentId) {
        return gymContextFacade.findEquipmentById(equipmentId)
                .map(Equipment::getEquipmentName)
                .filter(name -> !name.isBlank())
                .orElse("The machine");
    }

    private Date now() {
        return Date.from(clock.instant());
    }
}
