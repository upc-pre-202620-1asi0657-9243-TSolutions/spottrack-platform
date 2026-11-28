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
import com.spottrack.platform.maintenance.domain.model.commands.UpdateTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechnicianByTechnicianIdQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechnicianByUserIdQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechniciansByAdminQuery;
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
import com.spottrack.platform.maintenance.interfaces.rest.resources.UpdateTechnicalTicketResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.ModifyTicketStatusResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.RegisterMaintenanceCompletionResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.RequestMaintenanceResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.UpdateMaintenanceStatusResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.MaintenanceJobResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.MaintenanceLogResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.MaintenanceResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.TechnicalTicketResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.TechnicianResource;
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
import com.spottrack.platform.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Maintenance", description = "Maintenance requests, technical tickets, technicians and completion logs")
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

    @Operation(
            summary = "Request maintenance for an equipment",
            description = "Administrator-only. Opens a maintenance request for an equipment of one of the caller's gyms. "
                    + "Create a technical ticket from it with POST /api/v1/maintenance/tickets."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Maintenance request created",
                    content = @Content(schema = @Schema(implementation = MaintenanceResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceFailure"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
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

    @Operation(
            summary = "Create a technical ticket",
            description = "Administrator-only. Creates the technical ticket for a maintenance request owned by the caller. "
                    + "Only one ticket can exist per maintenance request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created",
                    content = @Content(schema = @Schema(implementation = TechnicalTicketResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceFailure"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
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

    @Operation(
            summary = "List the caller's technical tickets",
            description = "Administrator-only. Returns every ticket for equipment in the caller's gyms. "
                    + "/tickets and /tickets/me are equivalent."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TechnicalTicketResource.class)))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @GetMapping({"/tickets", "/tickets/me"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTickets(Authentication authentication) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var resources = gymContextFacade.findEquipmentSnapshotsByAdminUserId(adminUserId).stream()
                .flatMap(eq -> technicalTicketJpaRepository.findByEquipmentId(eq.equipmentId()).stream())
                .map(TechnicalTicketPersistenceAssembler::toDomainFromPersistence)
                .map(TechnicalTicketResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "Assign a ticket to a technician",
            description = "Administrator-only. Assigns the ticket to a technician of the caller's roster."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket assigned",
                    content = @Content(schema = @Schema(implementation = TechnicalTicketResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PatchMapping("/tickets/{ticketId}/assign/{technicianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignTechnicalTicket(Authentication authentication,
                                                   @Parameter(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90") @PathVariable String ticketId,
                                                   @Parameter(description = "Technician identifier (UUID)", example = "9b2e4c71-3f5a-4d8b-b6c0-1e7a2d9f4c35") @PathVariable String technicianId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var technicianError = checkTechnicianTenant(technicianId, adminUserId);
        if (technicianError.isPresent()) return technicianError.get();
        var command = new AssignTechnicalTicket(new TechnicalTicketId(ticketId), technicianId);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<TechnicalTicket, ApplicationError> s ->
                    ResponseEntity.ok(TechnicalTicketResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<TechnicalTicket, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @Operation(
            summary = "Edit a technical ticket",
            description = "Administrator-only. Partially updates description, priority, type and technician. "
                    + "Null fields are left unchanged; changing the technician does not change the status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket updated",
                    content = @Content(schema = @Schema(implementation = TechnicalTicketResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PatchMapping("/tickets/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTechnicalTicket(Authentication authentication,
                                                   @Parameter(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90") @PathVariable String ticketId,
                                                   @RequestBody UpdateTechnicalTicketResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkTicketOwnership(ticketId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var technicianError = checkTechnicianTenant(resource.technicianId(), adminUserId);
        if (technicianError.isPresent()) return technicianError.get();
        UpdateTechnicalTicket command;
        try {
            command = new UpdateTechnicalTicket(new TechnicalTicketId(ticketId), resource.description(),
                    resource.priority(), resource.type(), resource.technicianId());
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("TechnicalTicket", e.getMessage()));
        }
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<TechnicalTicket, ApplicationError> s ->
                    ResponseEntity.ok(TechnicalTicketResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<TechnicalTicket, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @Operation(
            summary = "Accept a maintenance job on behalf of a technician",
            description = "Administrator-only. Marks the job as accepted by a technician of the caller's roster."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job accepted",
                    content = @Content(schema = @Schema(implementation = MaintenanceJobResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PatchMapping("/jobs/{jobId}/accept/{technicianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> acceptMaintenance(Authentication authentication,
                                               @Parameter(description = "Maintenance job identifier (UUID)", example = "c4d8e2a1-6b3f-4a9c-8e57-2f1b0d6a9e48") @PathVariable String jobId,
                                               @Parameter(description = "Technician identifier (UUID)", example = "9b2e4c71-3f5a-4d8b-b6c0-1e7a2d9f4c35") @PathVariable String technicianId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkJobOwnership(jobId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var technicianError = checkTechnicianTenant(technicianId, adminUserId);
        if (technicianError.isPresent()) return technicianError.get();
        var command = new AcceptMaintenance(new MaintenanceJobId(jobId), technicianId);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<MaintenanceJob, ApplicationError> s ->
                    ResponseEntity.ok(MaintenanceJobResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<MaintenanceJob, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @Operation(
            summary = "Add a technician to the roster",
            description = "Administrator-only. Adds a technician without a login account to the caller's roster. "
                    + "To create a technician who can sign in, use POST /api/v1/users/technicians."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Technician created",
                    content = @Content(schema = @Schema(implementation = TechnicianResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceFailure"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PostMapping("/technicians")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTechnician(Authentication authentication,
                                              @RequestBody CreateTechnicianResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var command = CreateTechnicianCommandFromResourceAssembler.toCommandFromResource(resource, adminUserId);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Technician, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(TechnicianResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Technician, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @Operation(
            summary = "List the caller's technicians",
            description = "Administrator-only. Returns the technicians in the caller's roster."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Technicians found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TechnicianResource.class)))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @GetMapping("/technicians")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTechnicians(Authentication authentication) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var technicians = technicianQueryService.handle(new GetTechniciansByAdminQuery(adminUserId));
        var resources = technicians.stream()
                .map(TechnicianResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "List tickets assigned to the calling technician",
            description = "Technician-only. Returns the tickets assigned to the technician behind the bearer token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TechnicalTicketResource.class)))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @GetMapping("/tickets/assigned-to-me")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<?> getTicketsAssignedToMe(Authentication authentication) {
        var technician = resolveTechnician(authentication);
        if (technician.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Technician", authentication.getName()));
        }
        var resources = technicalTicketJpaRepository
                .findByTechnicianId(technician.get().getTechnicianId().uuid()).stream()
                .map(TechnicalTicketPersistenceAssembler::toDomainFromPersistence)
                .map(TechnicalTicketResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "Complete a ticket",
            description = "Administrators (for tickets of their gyms) or the assigned technician. "
                    + "Marks the ticket as resolved."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket completed",
                    content = @Content(schema = @Schema(implementation = TechnicalTicketResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PatchMapping("/tickets/{ticketId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public ResponseEntity<?> completeMaintenance(Authentication authentication,
                                                 @Parameter(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90") @PathVariable String ticketId) {
        var ownershipError = checkTicketAccess(ticketId, authentication);
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

    @Operation(
            summary = "Change a ticket status",
            description = "Administrators (for tickets of their gyms) or the assigned technician. "
                    + "Sets the ticket status (OPEN, IN_PROGRESS, RESOLVED, MODIFIED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed",
                    content = @Content(schema = @Schema(implementation = TechnicalTicketResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PatchMapping("/tickets/{ticketId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public ResponseEntity<?> modifyTicketStatus(Authentication authentication,
                                                @Parameter(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90") @PathVariable String ticketId,
                                                @RequestBody ModifyTicketStatusResource resource) {
        var ownershipError = checkTicketAccess(ticketId, authentication);
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

    @Operation(
            summary = "Register a completion log",
            description = "Administrators (for tickets of their gyms) or the assigned technician. "
                    + "Records the notes and cost of the work done on the ticket."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Completion log registered",
                    content = @Content(schema = @Schema(implementation = MaintenanceLogResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceFailure"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PostMapping("/tickets/{ticketId}/completion-log")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public ResponseEntity<?> registerMaintenanceCompletion(Authentication authentication,
                                                           @Parameter(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90") @PathVariable String ticketId,
                                                           @RequestBody RegisterMaintenanceCompletionResource resource) {
        var ownershipError = checkTicketAccess(ticketId, authentication);
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

    @Operation(
            summary = "Get the completion logs of a ticket",
            description = "Administrators (for tickets of their gyms) or the assigned technician."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MaintenanceLogResource.class)))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @GetMapping("/tickets/{ticketId}/completion-log")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public ResponseEntity<?> getMaintenanceCompletionLog(Authentication authentication,
                                                         @Parameter(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90") @PathVariable String ticketId) {
        var ownershipError = checkTicketAccess(ticketId, authentication);
        if (ownershipError.isPresent()) return ownershipError.get();
        var resources = maintenanceLogQueryService.handle(new GetMaintenanceLogsByTicketIdQuery(ticketId)).stream()
                .map(MaintenanceLogResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "List the caller's maintenance logs",
            description = "Administrator-only. Returns the completion logs of every ticket in the caller's gyms. "
                    + "/logs and /logs/me are equivalent."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MaintenanceLogResource.class)))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @GetMapping({"/logs", "/logs/me"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllMaintenanceLogs(Authentication authentication) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var resources = gymContextFacade.findEquipmentSnapshotsByAdminUserId(adminUserId).stream()
                .flatMap(eq -> technicalTicketJpaRepository.findByEquipmentId(eq.equipmentId()).stream())
                .flatMap(ticket -> maintenanceLogQueryService
                        .handle(new GetMaintenanceLogsByTicketIdQuery(ticket.getTicketId())).stream())
                .map(MaintenanceLogResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "Request a maintenance status change",
            description = "Administrator-only. Requests a change of the maintenance status (REQUESTED, IN_PROGRESS, "
                    + "COMPLETED) of the ticket."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Change requested",
                    content = @Content(schema = @Schema(implementation = TechnicalTicketResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PatchMapping("/tickets/{ticketId}/maintenance-status/request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> requestUpdateMaintenanceStatus(Authentication authentication,
                                                            @Parameter(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90") @PathVariable String ticketId,
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

    @Operation(
            summary = "Update the maintenance status",
            description = "Administrator-only. Sets the maintenance status (REQUESTED, IN_PROGRESS, COMPLETED) of the ticket."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = TechnicalTicketResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PatchMapping("/tickets/{ticketId}/maintenance-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMaintenanceStatus(Authentication authentication,
                                                     @Parameter(description = "Technical ticket identifier (UUID)", example = "5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90") @PathVariable String ticketId,
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

    @Operation(
            summary = "Recommend an equipment transfer",
            description = "Administrator-only. Records a recommendation to transfer or decommission an equipment of the "
                    + "caller's gyms. The response has no body."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recommendation recorded", content = @Content),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceFailure"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceForbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "MaintenanceNotFound")
    })
    @PostMapping("/equipment/{equipmentId}/transfer-recommendation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> recommendEquipmentTransfer(Authentication authentication,
                                                        @Parameter(description = "Equipment identifier (UUID)", example = "7e3a1c5d-9f2b-4e6a-b8d4-0c1f5a3e7b92") @PathVariable String equipmentId,
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

    private Optional<Technician> resolveTechnician(Authentication authentication) {
        return iamContextFacade.fetchUserIdByUsername(authentication.getName())
                .flatMap(userId -> technicianQueryService.handle(new GetTechnicianByUserIdQuery(userId)));
    }

    private Optional<ResponseEntity<?>> checkTicketAccess(String ticketId, Authentication authentication) {
        var isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) {
            var adminUserId = resolveAdminUserId(authentication);
            if (adminUserId == 0L) {
                return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Admin", authentication.getName())));
            }
            return checkTicketOwnership(ticketId, adminUserId);
        }
        var ticket = technicalTicketJpaRepository.findByTicketId(ticketId);
        if (ticket.isEmpty()) return Optional.of(ResponseEntity.notFound().build());
        var technician = resolveTechnician(authentication);
        if (technician.isEmpty()
                || !technician.get().getTechnicianId().uuid().equals(ticket.get().getTechnicianId())) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Maintenance", "ticketId:" + ticketId)));
        }
        return Optional.empty();
    }

    private Optional<ResponseEntity<?>> checkTechnicianTenant(String technicianId, Long adminUserId) {
        if (technicianId == null || technicianId.isBlank()) return Optional.empty();
        var technician = technicianQueryService.handle(new GetTechnicianByTechnicianIdQuery(technicianId));
        if (technician.isPresent() && technician.get().getAdminUserId() != null
                && !technician.get().getAdminUserId().equals(adminUserId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Maintenance", "technicianId:" + technicianId)));
        }
        return Optional.empty();
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
