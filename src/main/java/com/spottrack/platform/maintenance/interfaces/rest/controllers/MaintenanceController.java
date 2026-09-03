package com.spottrack.platform.maintenance.interfaces.rest.controllers;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.application.queryservices.MaintenanceLogQueryService;
import com.spottrack.platform.maintenance.application.queryservices.TechnicalTicketQueryService;
import com.spottrack.platform.maintenance.application.queryservices.TechnicianQueryService;
import com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance;
import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceJob;
import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceLog;
import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.commands.AcceptMaintenance;
import com.spottrack.platform.maintenance.domain.model.commands.AssignTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.commands.CompleteMaintenance;
import com.spottrack.platform.maintenance.domain.model.commands.RecommendEquipmentTransfer;
import com.spottrack.platform.maintenance.domain.model.commands.RegisterMaintenanceCompletion;
import com.spottrack.platform.maintenance.domain.model.commands.RequestUpdateMaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.commands.UpdateMaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllTechniciansQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetMaintenanceLogsByTicketIdQuery;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceJobId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers.TechnicalTicketPersistenceAssembler;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceJobJpaRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenancePersistenceRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.TechnicalTicketJpaRepository;
import com.spottrack.platform.maintenance.interfaces.rest.resources.CreateTechnicalTicketResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.CreateTechnicianResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.DecommissionEquipmentResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.ModifyTicketStatusResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.RegisterMaintenanceCompletionResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.RequestMaintenanceResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.UpdateMaintenanceStatusResource;
import com.spottrack.platform.maintenance.interfaces.rest.transform.CreateTechnicalTicketCommandFromResourceAssembler;
import com.spottrack.platform.maintenance.interfaces.rest.transform.CreateTechnicianCommandFromResourceAssembler;
import com.spottrack.platform.maintenance.interfaces.rest.transform.MaintenanceJobResourceFromEntityAssembler;
import com.spottrack.platform.maintenance.interfaces.rest.transform.MaintenanceLogResourceFromEntityAssembler;
import com.spottrack.platform.maintenance.interfaces.rest.transform.MaintenanceResourceFromEntityAssembler;
import com.spottrack.platform.maintenance.interfaces.rest.transform.RequestMaintenanceCommandFromResourceAssembler;
import com.spottrack.platform.maintenance.interfaces.rest.transform.TechnicalTicketResourceFromEntityAssembler;
import com.spottrack.platform.maintenance.interfaces.rest.transform.TechnicianResourceFromEntityAssembler;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Maintenance")
public class MaintenanceController {

    private final MaintenanceCommandService commandService;
    private final TechnicalTicketQueryService technicalTicketQueryService;
    private final MaintenanceLogQueryService maintenanceLogQueryService;
    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;
    private final TechnicalTicketJpaRepository technicalTicketJpaRepository;
    private final MaintenancePersistenceRepository maintenancePersistenceRepository;
    private final MaintenanceJobJpaRepository maintenanceJobJpaRepository;
    private final TechnicianQueryService technicianQueryService;

    public MaintenanceController(MaintenanceCommandService commandService,
                                 TechnicalTicketQueryService technicalTicketQueryService,
                                 MaintenanceLogQueryService maintenanceLogQueryService,
                                 GymContextFacade gymContextFacade,
                                 IamContextFacade iamContextFacade,
                                 TechnicalTicketJpaRepository technicalTicketJpaRepository,
                                 MaintenancePersistenceRepository maintenancePersistenceRepository,
                                 MaintenanceJobJpaRepository maintenanceJobJpaRepository,
                                 TechnicianQueryService technicianQueryService) {
        this.commandService = commandService;
        this.technicalTicketQueryService = technicalTicketQueryService;
        this.maintenanceLogQueryService = maintenanceLogQueryService;
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
        this.technicalTicketJpaRepository = technicalTicketJpaRepository;
        this.maintenancePersistenceRepository = maintenancePersistenceRepository;
        this.maintenanceJobJpaRepository = maintenanceJobJpaRepository;
        this.technicianQueryService = technicianQueryService;
    }

    @PostMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> requestMaintenance(Authentication authentication,
                                                @RequestBody RequestMaintenanceResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkEquipmentOwnership(resource.equipmentId(), adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = RequestMaintenanceCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Maintenance, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(MaintenanceResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Maintenance, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @PostMapping("/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTechnicalTicket(Authentication authentication,
                                                   @RequestBody CreateTechnicalTicketResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkMaintenanceOwnership(resource.maintenanceId(), adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = CreateTechnicalTicketCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<TechnicalTicket, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(TechnicalTicketResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<TechnicalTicket, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @GetMapping({"/tickets", "/tickets/me"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTickets(Authentication authentication) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var resources = gymContextFacade.findEquipmentsByAdminUserId(adminUserId).stream()
                .flatMap(eq -> technicalTicketJpaRepository.findByEquipmentId(eq.getId().uuid()).stream())
                .map(TechnicalTicketPersistenceAssembler::toDomainFromPersistence)
                .map(TechnicalTicketResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/tickets/{ticketId}/assign/{technicianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignTechnicalTicket(Authentication authentication,
                                                   @PathVariable String ticketId,
                                                   @PathVariable String technicianId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new AssignTechnicalTicket(new TechnicalTicketId(ticketId), technicianId);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<TechnicalTicket, ApplicationError> s ->
                    ResponseEntity.ok(TechnicalTicketResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<TechnicalTicket, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PatchMapping("/jobs/{jobId}/accept/{technicianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> acceptMaintenance(Authentication authentication,
                                               @PathVariable String jobId,
                                               @PathVariable String technicianId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkJobOwnership(jobId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new AcceptMaintenance(new MaintenanceJobId(jobId), technicianId);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<MaintenanceJob, ApplicationError> s ->
                    ResponseEntity.ok(MaintenanceJobResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<MaintenanceJob, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PostMapping("/technicians")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTechnician(@RequestBody CreateTechnicianResource resource) {
        var command = CreateTechnicianCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Technician, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(TechnicianResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Technician, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTechnicians() {
        var technicians = technicianQueryService.handle(new GetAllTechniciansQuery());
        var resources = technicians.stream()
                .map(TechnicianResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/tickets/{ticketId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> completeMaintenance(Authentication authentication,
                                                 @PathVariable String ticketId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new CompleteMaintenance(new TechnicalTicketId(ticketId));
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<TechnicalTicket, ApplicationError> s ->
                    ResponseEntity.ok(TechnicalTicketResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<TechnicalTicket, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PatchMapping("/tickets/{ticketId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> modifyTicketStatus(Authentication authentication,
                                                @PathVariable String ticketId,
                                                @RequestBody ModifyTicketStatusResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new com.spottrack.platform.maintenance.domain.model.commands.ModifyTicketStatus(
                new TechnicalTicketId(ticketId), resource.newStatus());
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<TechnicalTicket, ApplicationError> s ->
                    ResponseEntity.ok(TechnicalTicketResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<TechnicalTicket, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PostMapping("/tickets/{ticketId}/completion-log")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerMaintenanceCompletion(Authentication authentication,
                                                           @PathVariable String ticketId,
                                                           @RequestBody RegisterMaintenanceCompletionResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new RegisterMaintenanceCompletion(
                new TechnicalTicketId(ticketId),
                new MaintenanceId(resource.maintenanceId()),
                resource.notes(),
                resource.cost());
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<MaintenanceLog, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(MaintenanceLogResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<MaintenanceLog, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @GetMapping("/tickets/{ticketId}/completion-log")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMaintenanceCompletionLog(Authentication authentication,
                                                         @PathVariable String ticketId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var resources = maintenanceLogQueryService.handle(new GetMaintenanceLogsByTicketIdQuery(ticketId)).stream()
                .map(MaintenanceLogResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping({"/logs", "/logs/me"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllMaintenanceLogs(Authentication authentication) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var resources = gymContextFacade.findEquipmentsByAdminUserId(adminUserId).stream()
                .flatMap(eq -> technicalTicketJpaRepository.findByEquipmentId(eq.getId().uuid()).stream())
                .flatMap(ticket -> maintenanceLogQueryService
                        .handle(new GetMaintenanceLogsByTicketIdQuery(ticket.getTicketId())).stream())
                .map(MaintenanceLogResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/tickets/{ticketId}/maintenance-status/request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> requestUpdateMaintenanceStatus(Authentication authentication,
                                                            @PathVariable String ticketId,
                                                            @RequestBody UpdateMaintenanceStatusResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new RequestUpdateMaintenanceStatus(new TechnicalTicketId(ticketId), resource.newStatus());
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<TechnicalTicket, ApplicationError> s ->
                    ResponseEntity.ok(TechnicalTicketResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<TechnicalTicket, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PatchMapping("/tickets/{ticketId}/maintenance-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMaintenanceStatus(Authentication authentication,
                                                     @PathVariable String ticketId,
                                                     @RequestBody UpdateMaintenanceStatusResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new UpdateMaintenanceStatus(new TechnicalTicketId(ticketId), resource.newStatus());
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<TechnicalTicket, ApplicationError> s ->
                    ResponseEntity.ok(TechnicalTicketResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<TechnicalTicket, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PostMapping("/equipment/{equipmentId}/transfer-recommendation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> recommendEquipmentTransfer(Authentication authentication,
                                                        @PathVariable String equipmentId,
                                                        @RequestBody DecommissionEquipmentResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkEquipmentOwnership(equipmentId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new RecommendEquipmentTransfer(equipmentId, resource.reason());
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<String, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED).build();
            case Result.Failure<String, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    private Long resolveAdminUserId(Authentication authentication) {
        return iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
    }

    private Optional<ResponseEntity<?>> checkEquipmentOwnership(String equipmentId, Long adminUserId) {
        var gymId = gymContextFacade.resolveGymIdForEquipment(equipmentId);
        if (gymId.isEmpty()) return Optional.of(ResponseEntity.notFound().build());
        if (!gymContextFacade.isGymOwnedByAdmin(gymId.get(), adminUserId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Maintenance", "equipmentId:" + equipmentId)));
        }
        return Optional.empty();
    }

    private Optional<ResponseEntity<?>> checkMaintenanceOwnership(String maintenanceId, Long adminUserId) {
        var maintenance = maintenancePersistenceRepository.findByMaintenanceId(maintenanceId);
        if (maintenance.isEmpty()) return Optional.of(ResponseEntity.notFound().build());
        return checkEquipmentOwnership(maintenance.get().getEquipmentId(), adminUserId);
    }

    private Optional<ResponseEntity<?>> checkTicketOwnership(String ticketId, Long adminUserId) {
        var ticket = technicalTicketJpaRepository.findByTicketId(ticketId);
        if (ticket.isEmpty()) return Optional.of(ResponseEntity.notFound().build());
        return checkEquipmentOwnership(ticket.get().getEquipmentId(), adminUserId);
    }

    private Optional<ResponseEntity<?>> checkJobOwnership(String jobId, Long adminUserId) {
        var job = maintenanceJobJpaRepository.findByJobId(jobId);
        if (job.isEmpty()) return Optional.of(ResponseEntity.notFound().build());
        var maintenance = maintenancePersistenceRepository.findByMaintenanceId(job.get().getMaintenanceId());
        if (maintenance.isEmpty()) return Optional.of(ResponseEntity.notFound().build());
        return checkEquipmentOwnership(maintenance.get().getEquipmentId(), adminUserId);
    }
}
