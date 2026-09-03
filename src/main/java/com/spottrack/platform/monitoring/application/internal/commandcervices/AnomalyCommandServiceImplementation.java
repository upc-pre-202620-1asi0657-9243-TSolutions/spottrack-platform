package com.spottrack.platform.monitoring.application.internal.commandcervices;

import com.spottrack.platform.monitoring.application.commandServices.AnomalyCommandService;
import com.spottrack.platform.monitoring.domain.model.aggregates.Anomaly;
import com.spottrack.platform.monitoring.domain.model.commands.ReportAnomalyCommand;
import com.spottrack.platform.monitoring.domain.repositories.AnomalyRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class AnomalyCommandServiceImplementation implements AnomalyCommandService {
    private final AnomalyRepository anomalyRepository;
    public AnomalyCommandServiceImplementation(AnomalyRepository anomalyRepository){
        this.anomalyRepository = anomalyRepository;
    }

    @Override
    public Result<Anomaly, ApplicationError> handle(ReportAnomalyCommand command) {
        try {
            var anomaly = new Anomaly(command);
            var saved = anomalyRepository.save(anomaly);
            return Result.success(saved);
        } catch(IllegalArgumentException e ){
            return Result.failure(ApplicationError.validationError("Anomaly report", e.getMessage()));
        }
        catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Anomaly report", e.getMessage()));
        }

    }
}
