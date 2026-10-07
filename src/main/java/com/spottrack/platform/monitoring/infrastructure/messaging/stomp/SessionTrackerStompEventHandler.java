package com.spottrack.platform.monitoring.infrastructure.messaging.stomp;

import com.spottrack.platform.monitoring.domain.model.events.MotionCapturedEvent;
import com.spottrack.platform.monitoring.domain.model.events.UsageSessionEndedEvent;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionTrackerStompEventHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public SessionTrackerStompEventHandler(SimpMessagingTemplate messagingTemplate){
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void on(MotionCapturedEvent event) {
        if (event.movementDetectedViaSensor()) {
            publish(event.sessionTrackerId(), "ACTIVE");
        }
    }

    @EventListener
    public void on(UsageSessionEndedEvent event) {
        publish(event.sessionTrackerId(), "INACTIVE");
    }

    private void publish(SessionTrackerId sessionTrackerId, String status) {
        String id = sessionTrackerId.uuid();
        messagingTemplate.convertAndSend(
                "/topic/monitoring/session-trackers/" + id,
                new SessionTrackerStatusMessage(id, status)
        );
    }
}
