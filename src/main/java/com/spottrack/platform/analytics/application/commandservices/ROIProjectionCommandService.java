package com.spottrack.platform.analytics.application.commandservices;

import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.domain.model.commands.DetectHighDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.DetectLowDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.RecommendTransferCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestDowntimeCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestEarningsCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestRoiCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

import java.util.Optional;

public interface ROIProjectionCommandService {
    Optional<ROIProjection> handle(RequestRoiCommand command);
    Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, RequestDowntimeCostCommand command);
    Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, RequestEarningsCommand command);
    Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, DetectLowDemandSlotCommand command);
    Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, DetectHighDemandSlotCommand command);
    Result<ROIProjection, ApplicationError> handle(Long roiProjectionId, RecommendTransferCommand command);
}
