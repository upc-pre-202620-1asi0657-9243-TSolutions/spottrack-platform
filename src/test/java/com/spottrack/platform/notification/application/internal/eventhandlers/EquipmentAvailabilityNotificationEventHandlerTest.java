package com.spottrack.platform.notification.application.internal.eventhandlers;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.events.EquipmentStatusUpdatedIntegrationEvent;
import com.spottrack.platform.notification.application.commandservices.NotificationCommandService;
import com.spottrack.platform.notification.domain.model.commands.NotifyAvailabilityChangeCommand;
import com.spottrack.platform.notification.domain.model.valueobjects.AvailabilityChange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.spottrack.platform.notification.application.internal.eventhandlers.EquipmentAvailabilityNotificationEventHandler.toAvailabilityChange;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquipmentAvailabilityNotificationEventHandlerTest {

    @ParameterizedTest
    @CsvSource({
            "AVAILABLE, OCCUPIED, RELEASED",
            "AVAILABLE, RESERVED, RELEASED",
            "AVAILABLE, ACTIVE, RELEASED",
            "AVAILABLE, MAINTENANCE, RESTORED",
            "AVAILABLE, OUT_OF_SERVICE, RESTORED",
            "OCCUPIED, AVAILABLE, TAKEN",
            "RESERVED, AVAILABLE, TAKEN",
            "MAINTENANCE, AVAILABLE, BLOCKED",
            "OUT_OF_SERVICE, OCCUPIED, BLOCKED",
            "DECOMMISSIONED, MAINTENANCE, RETIRED"
    })
    void mapsEachStatusChangeToItsEffectOnTheFollowers(EquipmentStatus status, EquipmentStatus previous,
                                                       AvailabilityChange expected) {
        assertThat(toAvailabilityChange(status, previous)).contains(expected);
    }

    @Test
    void anUnchangedStatusAffectsNobody() {
        assertThat(toAvailabilityChange(EquipmentStatus.AVAILABLE, EquipmentStatus.AVAILABLE)).isEmpty();
    }

    @Test
    void forwardsTheChangeToTheNotificationService() {
        var service = mock(NotificationCommandService.class);

        new EquipmentAvailabilityNotificationEventHandler(service).on(
                new EquipmentStatusUpdatedIntegrationEvent("eq-1", EquipmentStatus.AVAILABLE, EquipmentStatus.MAINTENANCE));

        verify(service).handle(new NotifyAvailabilityChangeCommand("eq-1", AvailabilityChange.RESTORED));
    }

    @Test
    void ignoresStatusUpdatesThatChangeNothing() {
        var service = mock(NotificationCommandService.class);

        new EquipmentAvailabilityNotificationEventHandler(service).on(
                new EquipmentStatusUpdatedIntegrationEvent("eq-1", EquipmentStatus.OCCUPIED, EquipmentStatus.OCCUPIED));

        verify(service, never()).handle(any(NotifyAvailabilityChangeCommand.class));
    }

    @Test
    void aNotificationFailureNeverBreaksTheStatusChange() {
        var service = mock(NotificationCommandService.class);
        when(service.handle(any(NotifyAvailabilityChangeCommand.class))).thenThrow(new IllegalStateException("db down"));

        assertThatCode(() -> new EquipmentAvailabilityNotificationEventHandler(service).on(
                new EquipmentStatusUpdatedIntegrationEvent("eq-1", EquipmentStatus.AVAILABLE, EquipmentStatus.OCCUPIED)))
                .doesNotThrowAnyException();
    }
}
