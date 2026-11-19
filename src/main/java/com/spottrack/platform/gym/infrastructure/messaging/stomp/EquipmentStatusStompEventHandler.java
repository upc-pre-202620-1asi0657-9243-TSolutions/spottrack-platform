package com.spottrack.platform.gym.infrastructure.messaging.stomp;

import com.spottrack.platform.gym.domain.model.events.EquipmentStatusUpdatedEvent;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

/**
 * Pushes every equipment status change to the gym that owns the equipment, so a client
 * subscribed to {@code /topic/gyms/{gymId}/equipment} sees the live state of the whole floor
 * through a single subscription.
 *
 * <p>It covers every source of change (sensor sessions, reservations, maintenance, manual
 * updates) because all of them go through {@code Equipment.updateStatus}. The message is sent
 * after commit, so a client that re-reads the equipment on receipt already sees the new state.</p>
 */
@Slf4j
@Component
public class EquipmentStatusStompEventHandler {

    static final String TOPIC_PREFIX = "/topic/gyms/";
    static final String TOPIC_SUFFIX = "/equipment";

    private final SimpMessagingTemplate messagingTemplate;
    private final GymContextFacade gymContextFacade;

    public EquipmentStatusStompEventHandler(SimpMessagingTemplate messagingTemplate,
                                            GymContextFacade gymContextFacade) {
        this.messagingTemplate = messagingTemplate;
        this.gymContextFacade = gymContextFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(EquipmentStatusUpdatedEvent event) {
        var gymId = gymContextFacade.resolveGymIdForEquipment(event.equipmentId());
        if (gymId.isEmpty()) {
            log.debug("Equipment {} has no gym (no zone or branch); status change not broadcast", event.equipmentId());
            return;
        }
        messagingTemplate.convertAndSend(
                TOPIC_PREFIX + gymId.get() + TOPIC_SUFFIX,
                new EquipmentStatusMessage(event.equipmentId(), event.status().name(), Instant.now()));
    }
}
