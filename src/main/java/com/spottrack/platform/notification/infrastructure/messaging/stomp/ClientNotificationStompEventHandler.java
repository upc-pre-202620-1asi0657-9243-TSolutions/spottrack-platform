package com.spottrack.platform.notification.infrastructure.messaging.stomp;

import com.spottrack.platform.notification.domain.model.events.ClientNotificationCreatedEvent;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Delivers each client notification in real time to the sessions of that client only.
 *
 * <p>The client subscribes to {@code /user/queue/notifications}; Spring routes the message to the
 * sessions whose STOMP principal (set on CONNECT from the bearer token) is the client's email.
 * The notification is stored before this runs, so a client that was offline still finds it through
 * {@code GET /api/v1/notifications}.</p>
 */
@Slf4j
@Component
public class ClientNotificationStompEventHandler {

    static final String USER_DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;
    private final ProfilesContextFacade profilesContextFacade;

    public ClientNotificationStompEventHandler(SimpMessagingTemplate messagingTemplate,
                                               ProfilesContextFacade profilesContextFacade) {
        this.messagingTemplate = messagingTemplate;
        this.profilesContextFacade = profilesContextFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(ClientNotificationCreatedEvent event) {
        var email = profilesContextFacade.fetchClientEmailById(event.clientId());
        if (email.isEmpty()) {
            log.warn("Client {} has no email; notification {} stays in-app only", event.clientId(), event.notificationId());
            return;
        }
        messagingTemplate.convertAndSendToUser(email.get(), USER_DESTINATION, new ClientNotificationMessage(
                event.notificationId(), event.equipmentId(), event.type().name(), event.message(), event.createdAt()));
    }
}
