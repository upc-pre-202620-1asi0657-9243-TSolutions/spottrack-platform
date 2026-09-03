package com.spottrack.platform.analytics.application.internal.eventhandlers;

import com.spottrack.platform.analytics.application.commandservices.ROIProjectionCommandService;
import com.spottrack.platform.analytics.domain.model.commands.RecommendTransferCommand;
import com.spottrack.platform.analytics.domain.model.events.LowDemandSlotDetectedEvent;
import com.spottrack.platform.analytics.domain.repositories.ROIProjectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.spottrack.platform.shared.application.result.Result;

/**
 * Policy: [Low Demand Slot Detected] Recommend Transfer.
 *
 * targetZone isn't tracked anywhere in this ROIProjection model (it has no
 * equipmentId/zoneId at all), so it's left as a placeholder for an admin to
 * fill in later — same "no real business rule defined yet" situation as the
 * rest of this thin aggregate.
 *
 * The event only carries the business ROIProjectionId, but the command
 * service's handle(Long, ...) overloads key off the JPA primary key — so this
 * looks the aggregate back up by its business id first to get that key.
 */
@Component
@Slf4j
public class LowDemandSlotDetectedEventHandler {

    private final ROIProjectionRepository roiProjectionRepository;
    private final ROIProjectionCommandService roiProjectionCommandService;

    public LowDemandSlotDetectedEventHandler(ROIProjectionRepository roiProjectionRepository,
                                              ROIProjectionCommandService roiProjectionCommandService) {
        this.roiProjectionRepository = roiProjectionRepository;
        this.roiProjectionCommandService = roiProjectionCommandService;
    }

    @EventListener
    public void on(LowDemandSlotDetectedEvent event) {
        var found = roiProjectionRepository.findByRoiProjectionId(event.roiProjectionId());
        if (found.isEmpty()) {
            log.warn("Could not resolve ROI projection {} to auto-recommend a transfer", event.roiProjectionId().value());
            return;
        }
        var id = found.get().getId();

        var result = roiProjectionCommandService.handle(id, new RecommendTransferCommand(
                "Low demand detected for slot " + event.slotInfo() + " — consider relocating equipment",
                "unassigned"));

        if (result instanceof Result.Failure<?, ?> f) {
            log.warn("Failed to auto-recommend transfer for ROI projection {}: {}", id, f.error());
        }
    }
}
