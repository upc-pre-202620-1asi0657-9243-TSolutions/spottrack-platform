package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.domain.model.events.EquipmentAnomalyReportSubmitted;
import com.spottrack.platform.monitoring.interfaces.events.AnomalyReportSubmittedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Implements the "(Report Submitted) Send Report" policy from the event storming.
 * This context's responsibility ends at publishing the integration event below;
 * actually delivering the report (email, PDF, push notification, etc.) is not
 * this bounded context's job. Whoever owns that responsibility should add a
 * listener for {@link AnomalyReportSubmittedIntegrationEvent} (e.g. in a
 * Notifications bounded context) and implement the real delivery there.
 */
@Service
@Slf4j
public class AnomalyReportSubmittedEventHandler {
    private final ApplicationEventPublisher eventPublisher;

    public AnomalyReportSubmittedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(EquipmentAnomalyReportSubmitted event) {
        log.info("Anomaly report submitted for equipment {} in zone {}; publishing integration event for report delivery.",
                event.equipmentId(), event.zoneId());
        eventPublisher.publishEvent(new AnomalyReportSubmittedIntegrationEvent(
                event.reservationId(),
                event.equipmentId(),
                event.zoneId(),
                event.anomalyDescription(),
                event.emissionDate()
        ));
    }
}
