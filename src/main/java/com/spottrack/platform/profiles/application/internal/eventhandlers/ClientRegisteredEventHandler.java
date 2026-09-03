package com.spottrack.platform.profiles.application.internal.eventhandlers;

import com.spottrack.platform.profiles.domain.model.events.AdminRegisteredEvent;
import com.spottrack.platform.profiles.domain.model.events.ClientRegisteredEvent;
import com.spottrack.platform.profiles.interfaces.events.AdminRegisteredIntegrationEvent;
import com.spottrack.platform.profiles.interfaces.events.ClientRegisteredIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClientRegisteredEventHandler {
    private final ApplicationEventPublisher eventPublisher;

    public ClientRegisteredEventHandler(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(ClientRegisteredEvent event){
        eventPublisher.publishEvent(new ClientRegisteredIntegrationEvent(
                event.clientId(),
                null,
                event.firstName(),
                event.lastName(),
                event.email()
        ));
    }
}
