package com.spottrack.platform.gym.infrastructure.messaging.stomp;

import com.spottrack.platform.gym.domain.model.events.EquipmentStatusUpdatedEvent;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquipmentStatusStompEventHandlerTest {

    private SimpMessagingTemplate messagingTemplate;
    private GymContextFacade gymContextFacade;
    private EquipmentStatusStompEventHandler handler;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        gymContextFacade = mock(GymContextFacade.class);
        handler = new EquipmentStatusStompEventHandler(messagingTemplate, gymContextFacade);
    }

    @Test
    void publishesTheNewStatusOnTheTopicOfTheGymThatOwnsTheEquipment() {
        when(gymContextFacade.resolveGymIdForEquipment("eq-1")).thenReturn(Optional.of("gym-1"));

        handler.on(new EquipmentStatusUpdatedEvent("eq-1", EquipmentStatus.OCCUPIED, EquipmentStatus.AVAILABLE));

        var message = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/gyms/gym-1/equipment"), message.capture());
        assertThat(message.getValue()).isInstanceOfSatisfying(EquipmentStatusMessage.class, m -> {
            assertThat(m.equipmentId()).isEqualTo("eq-1");
            assertThat(m.status()).isEqualTo("OCCUPIED");
            assertThat(m.changedAt()).isNotNull();
        });
    }

    @Test
    void skipsEquipmentThatIsNotPlacedInAnyGym() {
        when(gymContextFacade.resolveGymIdForEquipment("eq-orphan")).thenReturn(Optional.empty());

        handler.on(new EquipmentStatusUpdatedEvent("eq-orphan", EquipmentStatus.AVAILABLE, EquipmentStatus.OCCUPIED));

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}
