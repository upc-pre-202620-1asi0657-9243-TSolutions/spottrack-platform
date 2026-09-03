package com.spottrack.platform.maintenance.application.commandServices;

import com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance;
import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceJob;
import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceLog;
import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.commands.AcceptMaintenance;
import com.spottrack.platform.maintenance.domain.model.commands.AssignTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.commands.CompleteMaintenance;
import com.spottrack.platform.maintenance.domain.model.commands.CreateMaintenanceJob;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnicalTicketCommand;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnician;
import com.spottrack.platform.maintenance.domain.model.commands.ModifyTicketStatus;
import com.spottrack.platform.maintenance.domain.model.commands.RecommendEquipmentTransfer;
import com.spottrack.platform.maintenance.domain.model.commands.RegisterMaintenanceCompletion;
import com.spottrack.platform.maintenance.domain.model.commands.RequestMaintenance;
import com.spottrack.platform.maintenance.domain.model.commands.RequestUpdateMaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.commands.UpdateMaintenanceStatus;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface MaintenanceCommandService {

    Result<Maintenance, ApplicationError> handle(RequestMaintenance command);

    Result<TechnicalTicket, ApplicationError> handle(CreateTechnicalTicketCommand command);

    Result<TechnicalTicket, ApplicationError> handle(AssignTechnicalTicket command);

    Result<MaintenanceJob, ApplicationError> handle(CreateMaintenanceJob command);
    Result<Technician, ApplicationError> handle(CreateTechnician command);

    Result<MaintenanceJob, ApplicationError> handle(AcceptMaintenance command);

    Result<TechnicalTicket, ApplicationError> handle(CompleteMaintenance command);

    Result<TechnicalTicket, ApplicationError> handle(ModifyTicketStatus command);

    Result<MaintenanceLog, ApplicationError> handle(RegisterMaintenanceCompletion command);

    Result<TechnicalTicket, ApplicationError> handle(RequestUpdateMaintenanceStatus command);

    Result<TechnicalTicket, ApplicationError> handle(UpdateMaintenanceStatus command);

    Result<String, ApplicationError> handle(RecommendEquipmentTransfer command);
}
