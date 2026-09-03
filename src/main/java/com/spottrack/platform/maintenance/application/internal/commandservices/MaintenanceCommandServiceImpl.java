package com.spottrack.platform.maintenance.application.internal.commandservices;

import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
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
import com.spottrack.platform.maintenance.domain.model.events.EquipmentTransferRecommendedEvent;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicianId;
import com.spottrack.platform.maintenance.domain.repositories.MaintenanceJobRepository;
import com.spottrack.platform.maintenance.domain.repositories.MaintenanceLogRepository;
import com.spottrack.platform.maintenance.domain.repositories.MaintenanceRepository;
import com.spottrack.platform.maintenance.domain.repositories.TechnicalTicketRepository;
import com.spottrack.platform.maintenance.domain.repositories.TechnicianRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceCommandServiceImpl implements MaintenanceCommandService {

    private final MaintenanceRepository maintenanceRepository;
    private final TechnicalTicketRepository technicalTicketRepository;
    private final MaintenanceJobRepository maintenanceJobRepository;
    private final TechnicianRepository technicianRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MaintenanceCommandServiceImpl(
            MaintenanceRepository maintenanceRepository,
            TechnicalTicketRepository technicalTicketRepository,
            MaintenanceJobRepository maintenanceJobRepository,
            TechnicianRepository technicianRepository,
            MaintenanceLogRepository maintenanceLogRepository,
            ApplicationEventPublisher eventPublisher) {
        this.maintenanceRepository = maintenanceRepository;
        this.technicalTicketRepository = technicalTicketRepository;
        this.maintenanceJobRepository = maintenanceJobRepository;
        this.technicianRepository = technicianRepository;
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Override
    public Result<Maintenance, ApplicationError> handle(RequestMaintenance command) {
        var maintenance = new Maintenance(command);
        var saved = maintenanceRepository.save(maintenance);
        return Result.success(saved);
    }

    @Transactional
    @Override
    public Result<TechnicalTicket, ApplicationError> handle(CreateTechnicalTicketCommand command) {
        try {
            var found = maintenanceRepository.findByMaintenanceId(new MaintenanceId(command.maintenanceId()));
            if (found.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Maintenance", command.maintenanceId()));
            }
            if (technicalTicketRepository.findByMaintenanceId(command.maintenanceId()).isPresent()) {
                return Result.failure(ApplicationError.conflict("TechnicalTicket", "a ticket already exists for maintenance " + command.maintenanceId()));
            }
            var maintenance = found.get();
            var ticket = new TechnicalTicket(
                    maintenance.getId().uuid(),
                    maintenance.getEquipmentId().uuid(),
                    maintenance.getDescription(),
                    command.priority(),
                    command.type());
            var saved = technicalTicketRepository.save(ticket);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("TechnicalTicket", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("TechnicalTicket creation", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<TechnicalTicket, ApplicationError> handle(AssignTechnicalTicket command) {
        if (!technicianRepository.existsById(new TechnicianId(command.technicianId()))) {
            return Result.failure(ApplicationError.notFound("Technician", command.technicianId()));
        }
        var found = technicalTicketRepository.findById(command.ticketId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("TechnicalTicket", command.ticketId().uuid()));
        }
        try {
            var ticket = found.get();
            ticket.assign(command);
            var saved = technicalTicketRepository.save(ticket);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("TechnicalTicket", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Technician, ApplicationError> handle(CreateTechnician command) {
        try {
            var technician = new Technician(command);
            var saved = technicianRepository.save(technician);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Technician", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Technician creation", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<MaintenanceJob, ApplicationError> handle(CreateMaintenanceJob command) {
        var job = new MaintenanceJob(command.maintenanceId());
        var saved = maintenanceJobRepository.save(job);
        return Result.success(saved);
    }

    @Transactional
    @Override
    public Result<MaintenanceJob, ApplicationError> handle(AcceptMaintenance command) {
        if (!technicianRepository.existsById(new TechnicianId(command.technicianId()))) {
            return Result.failure(ApplicationError.notFound("Technician", command.technicianId()));
        }
        var found = maintenanceJobRepository.findById(command.maintenanceJobId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MaintenanceJob", command.maintenanceJobId().uuid()));
        }
        try {
            var job = found.get();
            job.accept(command);
            var saved = maintenanceJobRepository.save(job);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("MaintenanceJob", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<TechnicalTicket, ApplicationError> handle(CompleteMaintenance command) {
        var found = technicalTicketRepository.findById(command.ticketId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("TechnicalTicket", command.ticketId().uuid()));
        }
        try {
            var ticket = found.get();
            ticket.markAsResolved();
            var saved = technicalTicketRepository.save(ticket);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("TechnicalTicket", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<TechnicalTicket, ApplicationError> handle(ModifyTicketStatus command) {
        var found = technicalTicketRepository.findById(command.ticketId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("TechnicalTicket", command.ticketId().uuid()));
        }
        try {
            var ticket = found.get();
            ticket.modifyStatus(command);
            var saved = technicalTicketRepository.save(ticket);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.validationError("TechnicalTicket", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<MaintenanceLog, ApplicationError> handle(RegisterMaintenanceCompletion command) {
        var log = new MaintenanceLog(command);
        var saved = maintenanceLogRepository.save(log);
        return Result.success(saved);
    }

    @Transactional
    @Override
    public Result<TechnicalTicket, ApplicationError> handle(RequestUpdateMaintenanceStatus command) {
        var found = technicalTicketRepository.findById(command.ticketId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("TechnicalTicket", command.ticketId().uuid()));
        }
        var ticket = found.get();
        ticket.requestStatusUpdate(command);
        var saved = technicalTicketRepository.save(ticket);
        return Result.success(saved);
    }

    @Transactional
    @Override
    public Result<TechnicalTicket, ApplicationError> handle(UpdateMaintenanceStatus command) {
        var found = technicalTicketRepository.findById(command.ticketId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("TechnicalTicket", command.ticketId().uuid()));
        }
        var ticket = found.get();
        ticket.updateMaintenanceStatus(command);
        var saved = technicalTicketRepository.save(ticket);
        return Result.success(saved);
    }

    @Transactional
    @Override
    public Result<String, ApplicationError> handle(RecommendEquipmentTransfer command) {
        eventPublisher.publishEvent(new EquipmentTransferRecommendedEvent(command.equipmentId(), command.reason()));
        return Result.success(command.equipmentId());
    }
}
