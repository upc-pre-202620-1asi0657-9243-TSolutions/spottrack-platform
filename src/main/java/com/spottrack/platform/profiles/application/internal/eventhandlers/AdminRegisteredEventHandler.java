package com.spottrack.platform.profiles.application.internal.eventhandlers;

import com.spottrack.platform.profiles.domain.model.events.AdminRegisteredEvent;
import com.spottrack.platform.profiles.interfaces.events.AdminRegisteredIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminRegisteredEventHandler {
    private final ApplicationEventPublisher eventPublisher;

    public AdminRegisteredEventHandler(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(AdminRegisteredEvent event){
        eventPublisher.publishEvent(new AdminRegisteredIntegrationEvent(
                event.adminId(),
                null,
                event.firstName(),
                event.lastName(),
                event.email()
        ));
    }
}
