package com.spottrack.platform.analytics.application.internal.eventhandlers;

import com.spottrack.platform.analytics.application.commandservices.ROIProjectionCommandService;
import com.spottrack.platform.analytics.domain.model.commands.DetectHighDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.DetectLowDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.events.ROIProjectionGeneratedEvent;
import com.spottrack.platform.analytics.domain.repositories.ROIProjectionRepository;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Policy: [ROI Projection Generated] Negative ROI? Detect Low Demand Slot :
 * Positive ROI? Detect High Demand Slot.
 *
 * ROIProjection doesn't track a time slot or user estimate anywhere yet, so
 * both are derived/placeholder here — same "no real business rule defined
 * yet" situation as DOWNTIME_COST_PER_MINUTE in ROIProjectionCommandServiceImpl.
 *
 * The event only carries the business ROIProjectionId, but the command
 * service's handle(Long, ...) overloads key off the JPA primary key — so this
 * looks the aggregate back up by its business id first to get that key.
 */
@Component
@Slf4j
public class ROIProjectionGeneratedEventHandler {

    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");

    private final ROIProjectionRepository roiProjectionRepository;
    private final ROIProjectionCommandService roiProjectionCommandService;

    public ROIProjectionGeneratedEventHandler(ROIProjectionRepository roiProjectionRepository,
                                               ROIProjectionCommandService roiProjectionCommandService) {
        this.roiProjectionRepository = roiProjectionRepository;
        this.roiProjectionCommandService = roiProjectionCommandService;
    }

    @EventListener
    public void on(ROIProjectionGeneratedEvent event) {
        if (event.roiIndex() == 0) return;

        var found = roiProjectionRepository.findByRoiProjectionId(event.roiProjectionId());
        if (found.isEmpty()) {
            log.warn("Could not resolve ROI projection {} to auto-detect its demand slot", event.roiProjectionId().value());
            return;
        }
        var id = found.get().getId();
        var timeSlot = LocalDateTime.now().format(SLOT_FORMAT);

        Result<?, ?> result = event.roiIndex() < 0
                ? roiProjectionCommandService.handle(id, new DetectLowDemandSlotCommand(timeSlot, 0))
                : roiProjectionCommandService.handle(id, new DetectHighDemandSlotCommand(timeSlot, 0));

        if (result instanceof Result.Failure<?, ?> f) {
            log.warn("Failed to auto-detect demand slot for ROI projection {}: {}", id, f.error());
        }
    }
}
