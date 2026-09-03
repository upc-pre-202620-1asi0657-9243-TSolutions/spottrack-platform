package com.spottrack.platform.analytics.application.internal.commandservices;

import com.spottrack.platform.analytics.application.commandservices.MaintenanceQuoteCommandService;
import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.domain.model.commands.RequestADetailedMaintenanceQuoteCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestMaintenanceCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestPreventiveCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestSparePartsCommand;
import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;
import com.spottrack.platform.analytics.domain.repositories.MaintenanceQuoteRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MaintenanceQuoteCommandServiceImpl implements MaintenanceQuoteCommandService {

    private final MaintenanceQuoteRepository maintenanceQuoteRepository;

    public MaintenanceQuoteCommandServiceImpl(MaintenanceQuoteRepository maintenanceQuoteRepository) {
        this.maintenanceQuoteRepository = maintenanceQuoteRepository;
    }

    public Optional<MaintenanceQuote> handle(RequestADetailedMaintenanceQuoteCommand command) {
        var maintenanceQuoteId = new MaintenanceQuoteId(
            ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
        var maintenanceQuote = new MaintenanceQuote(maintenanceQuoteId, command.equipmentId());
        maintenanceQuote.updateCorrectiveActionsCost(command.amount());
        this.maintenanceQuoteRepository.save(maintenanceQuote);
        return Optional.of(maintenanceQuote);
    }

    @Override
    public Result<MaintenanceQuote, ApplicationError> handle(Long maintenanceQuoteId, RequestSparePartsCommand command) {
        var found = maintenanceQuoteRepository.findById(maintenanceQuoteId);
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MaintenanceQuote", maintenanceQuoteId.toString()));
        }
        var quote = found.get();
        quote.updateSparePartsCost(command.quantity() * command.unitPrice());
        return Result.success(maintenanceQuoteRepository.save(quote));
    }

    @Override
    public Result<MaintenanceQuote, ApplicationError> handle(Long maintenanceQuoteId, RequestPreventiveCostCommand command) {
        var found = maintenanceQuoteRepository.findById(maintenanceQuoteId);
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MaintenanceQuote", maintenanceQuoteId.toString()));
        }
        var quote = found.get();
        quote.updatePreventiveCost(command.amount());
        return Result.success(maintenanceQuoteRepository.save(quote));
    }

    @Override
    public Result<MaintenanceQuote, ApplicationError> handle(Long maintenanceQuoteId, RequestMaintenanceCostCommand command) {
        var found = maintenanceQuoteRepository.findById(maintenanceQuoteId);
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MaintenanceQuote", maintenanceQuoteId.toString()));
        }
        var quote = found.get();
        quote.calculateTotalMaintenanceCost(command.amount());
        return Result.success(maintenanceQuoteRepository.save(quote));
    }
}
