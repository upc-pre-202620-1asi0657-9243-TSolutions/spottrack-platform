package com.spottrack.platform.notification.infrastructure.messaging.stomp;

import com.spottrack.platform.notification.domain.model.events.ClientNotificationCreatedEvent;
import com.spottrack.platform.notification.domain.model.valueobjects.NotificationType;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientNotificationStompEventHandlerTest {

    private SimpMessagingTemplate messagingTemplate;
    private ProfilesContextFacade profiles;
    private ClientNotificationStompEventHandler handler;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        profiles = mock(ProfilesContextFacade.class);
        handler = new ClientNotificationStompEventHandler(messagingTemplate, profiles);
    }

    @Test
    void sendsTheNotificationOnlyToTheSessionsOfTheClient() {
        when(profiles.fetchClientEmailById(7L)).thenReturn(Optional.of("ana@spottrack.com"));

        handler.on(new ClientNotificationCreatedEvent(3L, 7L, "eq-1", NotificationType.EQUIPMENT_AVAILABLE,
                "Leg Press is free now.", new Date()));

        var payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(eq("ana@spottrack.com"), eq("/queue/notifications"), payload.capture());
        assertThat(payload.getValue()).isInstanceOfSatisfying(ClientNotificationMessage.class, m -> {
            assertThat(m.id()).isEqualTo(3L);
            assertThat(m.equipmentId()).isEqualTo("eq-1");
            assertThat(m.type()).isEqualTo("EQUIPMENT_AVAILABLE");
            assertThat(m.message()).isEqualTo("Leg Press is free now.");
        });
    }

    @Test
    void keepsTheNotificationInAppOnlyWhenTheClientCannotBeResolved() {
        when(profiles.fetchClientEmailById(7L)).thenReturn(Optional.empty());

        handler.on(new ClientNotificationCreatedEvent(3L, 7L, "eq-1", NotificationType.EQUIPMENT_RESTORED,
                "Leg Press is back in service.", new Date()));

        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any(Object.class));
    }
}
