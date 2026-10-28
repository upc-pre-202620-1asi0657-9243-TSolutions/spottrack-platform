package com.spottrack.platform.maintenance.application.acl;

import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnician;
import com.spottrack.platform.maintenance.interfaces.acl.MaintenanceContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceContextFacadeImpl implements MaintenanceContextFacade {

    private final MaintenanceCommandService maintenanceCommandService;

    public MaintenanceContextFacadeImpl(MaintenanceCommandService maintenanceCommandService) {
        this.maintenanceCommandService = maintenanceCommandService;
    }

    @Override
    public Result<String, ApplicationError> provisionTechnician(Long userId, Long adminUserId, String name) {
        return switch (maintenanceCommandService.handle(new CreateTechnician(name, userId, adminUserId))) {
            case Result.Success<?, ApplicationError> s ->
                    Result.success(((com.spottrack.platform.maintenance.domain.model.aggregates.Technician) s.value())
                            .getTechnicianId().uuid());
            case Result.Failure<?, ApplicationError> f -> Result.failure(f.error());
        };
    }
}
