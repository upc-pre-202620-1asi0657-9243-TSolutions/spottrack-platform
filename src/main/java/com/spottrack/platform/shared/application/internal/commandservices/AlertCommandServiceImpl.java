package com.spottrack.platform.shared.application.internal.commandservices;

import com.spottrack.platform.shared.application.commandservices.AlertCommandService;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.commands.ClearAlertCommand;
import com.spottrack.platform.shared.domain.model.commands.ClearResolvedAlertsCommand;
import com.spottrack.platform.shared.domain.model.commands.CreateAlertCommand;
import com.spottrack.platform.shared.domain.model.commands.ResolveAlertCommand;
import com.spottrack.platform.shared.domain.repositories.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AlertCommandServiceImpl implements AlertCommandService {

    private final AlertRepository alertRepository;

    public AlertCommandServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Result<Alert, ApplicationError> handle(CreateAlertCommand command) {
        try {
            var alert = new Alert(null, command.adminUserId(), command.equipmentId(), command.severity(),
                    command.source(), command.message(), false, null, null, new Date());
            var saved = alertRepository.save(alert);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Alert", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Alert creation", e.getMessage()));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Result<Alert, ApplicationError> handle(ResolveAlertCommand command) {
        try {
            var alertOpt = alertRepository.findById(command.alertId());
            if (alertOpt.isEmpty() || alertOpt.get().isCleared()) {
                return Result.failure(ApplicationError.notFound("Alert", command.alertId().toString()));
            }
            var alert = alertOpt.get();
            if (!alert.getAdminUserId().equals(command.adminUserId())) {
                return Result.failure(ApplicationError.forbidden("Alert", "alertId:" + command.alertId()));
            }
            alert.resolve(new Date());
            var saved = alertRepository.save(alert);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Alert resolution", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Alert, ApplicationError> handle(ClearAlertCommand command) {
        try {
            var alertOpt = alertRepository.findById(command.alertId());
            if (alertOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Alert", command.alertId().toString()));
            }
            var alert = alertOpt.get();
            if (!alert.getAdminUserId().equals(command.adminUserId())) {
                return Result.failure(ApplicationError.forbidden("Alert", "alertId:" + command.alertId()));
            }
            if (alert.isCleared()) {
                return Result.success(alert);
            }
            if (!alert.isResolved()) {
                return Result.failure(pendingAlertCannotBeCleared());
            }
            alert.clear(new Date());
            return Result.success(alertRepository.save(alert));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Alert clearing", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Integer, ApplicationError> handle(ClearResolvedAlertsCommand command) {
        try {
            var resolved = alertRepository.findAllNotClearedByAdminUserId(command.adminUserId(), true);
            var now = new Date();
            for (var alert : resolved) {
                alert.clear(now);
                alertRepository.save(alert);
            }
            return Result.success(resolved.size());
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Alert clearing", e.getMessage()));
        }
    }

    private static ApplicationError pendingAlertCannotBeCleared() {
        return ApplicationError.businessRuleViolation("Alert clearing",
                "Only resolved alerts can be cleared; resolve the alert first.");
    }
}
