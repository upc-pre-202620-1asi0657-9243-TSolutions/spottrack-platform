package com.spottrack.platform.notification.application.internal.eventhandlers;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.events.EquipmentStatusUpdatedIntegrationEvent;
import com.spottrack.platform.notification.application.commandservices.NotificationCommandService;
import com.spottrack.platform.notification.domain.model.commands.NotifyAvailabilityChangeCommand;
import com.spottrack.platform.notification.domain.model.valueobjects.AvailabilityChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;
import java.util.Set;

/**
 * Policy: [Equipment Status Updated] Notify the clients following the equipment.
 *
 * <p>Every source of change (reservations ending or expiring, sensor sessions, maintenance tickets,
 * manual updates) goes through the equipment status, so this single listener covers both a machine
 * being released (US12) and a machine coming back from maintenance (US23). It runs after commit so
 * a failure here never rolls back the status change itself.</p>
 */
@Slf4j
@Component
public class EquipmentAvailabilityNotificationEventHandler {

    private static final Set<EquipmentStatus> IN_USE = Set.of(
            EquipmentStatus.OCCUPIED, EquipmentStatus.RESERVED, EquipmentStatus.ACTIVE);
    private static final Set<EquipmentStatus> OUT_OF_ORDER = Set.of(
            EquipmentStatus.MAINTENANCE, EquipmentStatus.OUT_OF_SERVICE);

    private final NotificationCommandService notificationCommandService;

    public EquipmentAvailabilityNotificationEventHandler(NotificationCommandService notificationCommandService) {
        this.notificationCommandService = notificationCommandService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(EquipmentStatusUpdatedIntegrationEvent event) {
        var change = toAvailabilityChange(event.status(), event.previousStatus());
        if (change.isEmpty()) {
            return;
        }
        try {
            notificationCommandService.handle(new NotifyAvailabilityChangeCommand(event.equipmentId(), change.get()));
        } catch (RuntimeException e) {
            log.error("Could not notify the clients following equipment {} ({}): {}",
                    event.equipmentId(), change.get(), e.getMessage(), e);
        }
    }

    static Optional<AvailabilityChange> toAvailabilityChange(EquipmentStatus status, EquipmentStatus previousStatus) {
        if (status == null || status == previousStatus) {
            return Optional.empty();
        }
        if (status == EquipmentStatus.AVAILABLE) {
            return Optional.of(OUT_OF_ORDER.contains(previousStatus)
                    ? AvailabilityChange.RESTORED
                    : AvailabilityChange.RELEASED);
        }
        if (IN_USE.contains(status)) {
            return Optional.of(AvailabilityChange.TAKEN);
        }
        if (OUT_OF_ORDER.contains(status)) {
            return Optional.of(AvailabilityChange.BLOCKED);
        }
        if (status == EquipmentStatus.DECOMMISSIONED) {
            return Optional.of(AvailabilityChange.RETIRED);
        }
        return Optional.empty();
    }
}
