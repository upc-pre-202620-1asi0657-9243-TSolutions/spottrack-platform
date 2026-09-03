package com.spottrack.platform.analytics.application.commandservices;

import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.domain.model.commands.RequestADetailedMaintenanceQuoteCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestMaintenanceCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestPreventiveCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestSparePartsCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

import java.util.Optional;

public interface MaintenanceQuoteCommandService {
    Optional<MaintenanceQuote> handle(RequestADetailedMaintenanceQuoteCommand command);
    Result<MaintenanceQuote, ApplicationError> handle(Long maintenanceQuoteId, RequestSparePartsCommand command);
    Result<MaintenanceQuote, ApplicationError> handle(Long maintenanceQuoteId, RequestPreventiveCostCommand command);
    Result<MaintenanceQuote, ApplicationError> handle(Long maintenanceQuoteId, RequestMaintenanceCostCommand command);
}
