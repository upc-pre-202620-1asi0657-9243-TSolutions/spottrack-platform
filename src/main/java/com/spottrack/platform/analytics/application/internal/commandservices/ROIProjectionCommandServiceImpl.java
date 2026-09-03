package com.spottrack.platform.analytics.application.internal.commandservices;

import com.spottrack.platform.analytics.application.commandservices.ROIProjectionCommandService;
import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.domain.model.commands.DetectHighDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.DetectLowDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.RecommendTransferCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestDowntimeCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestEarningsCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestRoiCommand;
import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;
import com.spottrack.platform.analytics.domain.repositories.ROIProjectionRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ROIProjectionCommandServiceImpl implements ROIProjectionCommandService {

    /** Same placeholder rate used by ActivityReport — no real business rule defined yet. */
    private static final double DOWNTIME_COST_PER_MINUTE = 2.0;

    private final ROIProjectionRepository roiProjectionRepository;

    public ROIProjectionCommandServiceImpl(ROIProjectionRepository roiProjectionRepository) {
        this.roiProjectionRepository = roiProjectionRepository;
    }

    public Optional<ROIProjection> handle(RequestRoiCommand command) {
        var roiProjectionId = new ROIProjectionId(
            ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
        var roiProjection = new ROIProjection(roiProjectionId);
        roiProjection.generateROIProjection(command.expectedRoiPercentage());
        this.roiProjectionRepository.save(roiProjection);
        return Optional.of(roiProjection);
    }

    @Override
    public Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, RequestDowntimeCostCommand command) {
        return withExisting(roiProjectionId, roi ->
                roi.updateDowntimeCost(Math.round(command.minutesInactive() * DOWNTIME_COST_PER_MINUTE) * 1.0));
    }

    @Override
    public Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, RequestEarningsCommand command) {
        return withExisting(roiProjectionId, roi -> roi.updateEarnings(command.projectedRevenue()));
    }

    @Override
    public Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, DetectLowDemandSlotCommand command) {
        return withExisting(roiProjectionId, roi ->
                roi.markAsLowDemand(command.timeSlot() + " (" + command.estimatedUsers() + " estimated users)"));
    }

    @Override
    public Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, DetectHighDemandSlotCommand command) {
        return withExisting(roiProjectionId, roi ->
                roi.markAsHighDemand(command.timeSlot() + " (" + command.estimatedUsers() + " estimated users)"));
    }

    @Override
    public Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, RecommendTransferCommand command) {
        return withExisting(roiProjectionId, roi ->
                roi.recommendEquipmentTransfer(command.operationalAdvice() + " -> " + command.targetZone()));
    }

    private Result<ROIProjection, ApplicationError> withExisting(Long roiProjectionId, java.util.function.Consumer<ROIProjection> mutate) {
        var found = roiProjectionRepository.findById(roiProjectionId);
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("ROIProjection", roiProjectionId.toString()));
        }
        var roiProjection = found.get();
        mutate.accept(roiProjection);
        return Result.success(roiProjectionRepository.save(roiProjection));
    }
}
