package com.spottrack.platform.maintenance.interfaces.rest.controllers;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.application.queryservices.MaintenanceLogQueryService;
import com.spottrack.platform.maintenance.application.queryservices.TechnicalTicketQueryService;
import com.spottrack.platform.maintenance.application.queryservices.TechnicianQueryService;
import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.commands.AssignTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.commands.CompleteMaintenance;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnician;
import com.spottrack.platform.maintenance.domain.model.commands.ModifyTicketStatus;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechnicianByTechnicianIdQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechnicianByUserIdQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechniciansByAdminQuery;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.TechnicalTicketPersistenceEntity;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceJobJpaRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenancePersistenceRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.TechnicalTicketJpaRepository;
import com.spottrack.platform.maintenance.interfaces.rest.resources.CreateTechnicianResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.ModifyTicketStatusResource;
import com.spottrack.platform.maintenance.interfaces.rest.resources.TechnicianResource;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaintenanceControllerTechnicianAccessTest {

    private static final String TECH_EMAIL = "tecnico@gym.com";
    private static final String ADMIN_EMAIL = "admin@gym.com";
    private static final Long TECH_USER_ID = 55L;
    private static final Long ADMIN_USER_ID = 42L;
    private static final String TECH_ID = "tech-1";
    private static final String OWN_TICKET = "ticket-own";
    private static final String OTHER_TICKET = "ticket-other";

    private MaintenanceCommandService commandService;
    private IamContextFacade iamContextFacade;
    private GymContextFacade gymContextFacade;
    private TechnicalTicketJpaRepository ticketJpaRepository;
    private TechnicianQueryService technicianQueryService;
    private MaintenanceController controller;

    @BeforeEach
    void setUp() {
        commandService = mock(MaintenanceCommandService.class);
        iamContextFacade = mock(IamContextFacade.class);
        gymContextFacade = mock(GymContextFacade.class);
        ticketJpaRepository = mock(TechnicalTicketJpaRepository.class);
        technicianQueryService = mock(TechnicianQueryService.class);
        controller = new MaintenanceController(commandService, mock(TechnicalTicketQueryService.class),
                mock(MaintenanceLogQueryService.class), gymContextFacade, iamContextFacade, ticketJpaRepository,
                mock(MaintenancePersistenceRepository.class), mock(MaintenanceJobJpaRepository.class),
                technicianQueryService);

        when(iamContextFacade.fetchUserIdByUsername(TECH_EMAIL)).thenReturn(Optional.of(TECH_USER_ID));
        when(iamContextFacade.fetchUserIdByUsername(ADMIN_EMAIL)).thenReturn(Optional.of(ADMIN_USER_ID));
        when(technicianQueryService.handle(new GetTechnicianByUserIdQuery(TECH_USER_ID)))
                .thenReturn(Optional.of(new Technician(TECH_ID, "Maria Perez", TECH_USER_ID, ADMIN_USER_ID)));
        when(ticketJpaRepository.findByTicketId(OWN_TICKET)).thenReturn(Optional.of(ticketEntity(OWN_TICKET, TECH_ID)));
        when(ticketJpaRepository.findByTicketId(OTHER_TICKET))
                .thenReturn(Optional.of(ticketEntity(OTHER_TICKET, "tech-2")));
    }

    @Test
    void technicianListsOnlyTicketsAssignedToThem() {
        when(ticketJpaRepository.findByTechnicianId(TECH_ID)).thenReturn(List.of(ticketEntity(OWN_TICKET, TECH_ID)));

        var response = controller.getTicketsAssignedToMe(technician());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, ((List<?>) response.getBody()).size());
        verify(ticketJpaRepository).findByTechnicianId(TECH_ID);
        verify(ticketJpaRepository, never()).findAll();
    }

    @Test
    void userWithoutRosterEntryGetsNotFoundOnAssignedToMe() {
        when(technicianQueryService.handle(new GetTechnicianByUserIdQuery(TECH_USER_ID))).thenReturn(Optional.empty());

        var response = controller.getTicketsAssignedToMe(technician());

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void technicianChangesStatusOfOwnTicket() {
        var ticket = new TechnicalTicket(OWN_TICKET, "m-1", "eq-1", TECH_ID, "d", TicketPriority.HIGH,
                TicketType.CORRECTIVE, "IN_PROGRESS", "REQUESTED", null, null);
        when(commandService.handle(any(ModifyTicketStatus.class))).thenReturn(Result.success(ticket));

        var response = controller.modifyTicketStatus(technician(), OWN_TICKET,
                new ModifyTicketStatusResource(TicketStatus.RESOLVED));

        assertEquals(200, response.getStatusCode().value());
        verify(commandService).handle(any(ModifyTicketStatus.class));
    }

    @Test
    void technicianCompletesOwnTicket() {
        var ticket = new TechnicalTicket(OWN_TICKET, "m-1", "eq-1", TECH_ID, "d", TicketPriority.HIGH,
                TicketType.CORRECTIVE, "RESOLVED", "REQUESTED", null, null);
        when(commandService.handle(any(CompleteMaintenance.class))).thenReturn(Result.success(ticket));

        var response = controller.completeMaintenance(technician(), OWN_TICKET);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void technicianCannotTouchTicketAssignedToSomeoneElse() {
        var status = controller.modifyTicketStatus(technician(), OTHER_TICKET,
                new ModifyTicketStatusResource(TicketStatus.RESOLVED));
        var complete = controller.completeMaintenance(technician(), OTHER_TICKET);
        var log = controller.getMaintenanceCompletionLog(technician(), OTHER_TICKET);

        for (var response : List.of(status, complete, log)) {
            assertEquals(403, response.getStatusCode().value());
            assertEquals("FORBIDDEN", assertInstanceOf(ErrorResource.class, response.getBody()).code());
        }
        verify(commandService, never()).handle(any(ModifyTicketStatus.class));
        verify(commandService, never()).handle(any(CompleteMaintenance.class));
    }

    @Test
    void technicianWithoutRosterEntryIsForbiddenOnAnyTicket() {
        when(technicianQueryService.handle(new GetTechnicianByUserIdQuery(TECH_USER_ID))).thenReturn(Optional.empty());

        var response = controller.completeMaintenance(technician(), OWN_TICKET);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void technicianGetsNotFoundForUnknownTicket() {
        when(ticketJpaRepository.findByTicketId("missing")).thenReturn(Optional.empty());

        var response = controller.completeMaintenance(technician(), "missing");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void adminKeepsOwnershipCheckByGym() {
        when(gymContextFacade.resolveGymIdForEquipment("eq-1")).thenReturn(Optional.of("gym-1"));
        when(gymContextFacade.isGymOwnedByAdmin("gym-1", ADMIN_USER_ID)).thenReturn(false);

        var response = controller.completeMaintenance(admin(), OWN_TICKET);

        assertEquals(403, response.getStatusCode().value());
        verify(commandService, never()).handle(any(CompleteMaintenance.class));
    }

    @Test
    void adminOfTheGymCanCompleteTheTicket() {
        when(gymContextFacade.resolveGymIdForEquipment("eq-1")).thenReturn(Optional.of("gym-1"));
        when(gymContextFacade.isGymOwnedByAdmin("gym-1", ADMIN_USER_ID)).thenReturn(true);
        var ticket = new TechnicalTicket(OWN_TICKET, "m-1", "eq-1", TECH_ID, "d", TicketPriority.HIGH,
                TicketType.CORRECTIVE, "RESOLVED", "REQUESTED", null, null);
        when(commandService.handle(any(CompleteMaintenance.class))).thenReturn(Result.success(ticket));

        var response = controller.completeMaintenance(admin(), OWN_TICKET);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void assignRejectsTechnicianOwnedByAnotherAdmin() {
        allowAdminOverTicket();
        when(technicianQueryService.handle(new GetTechnicianByTechnicianIdQuery("tech-foreign")))
                .thenReturn(Optional.of(new Technician("tech-foreign", "Otro", 77L, 999L)));

        var response = controller.assignTechnicalTicket(admin(), OWN_TICKET, "tech-foreign");

        assertEquals(403, response.getStatusCode().value());
        verify(commandService, never()).handle(any(AssignTechnicalTicket.class));
    }

    @Test
    void assignAllowsTechnicianOfTheSameAdminAndLegacyTechnicians() {
        allowAdminOverTicket();
        when(technicianQueryService.handle(new GetTechnicianByTechnicianIdQuery("tech-legacy")))
                .thenReturn(Optional.of(new Technician("tech-legacy", "Carlos Seed")));
        when(technicianQueryService.handle(new GetTechnicianByTechnicianIdQuery(TECH_ID)))
                .thenReturn(Optional.of(new Technician(TECH_ID, "Maria Perez", TECH_USER_ID, ADMIN_USER_ID)));
        var ticket = new TechnicalTicket(OWN_TICKET, "m-1", "eq-1", TECH_ID, "d", TicketPriority.HIGH,
                TicketType.CORRECTIVE, "IN_PROGRESS", "REQUESTED", null, null);
        when(commandService.handle(any(AssignTechnicalTicket.class))).thenReturn(Result.success(ticket));

        assertEquals(200, controller.assignTechnicalTicket(admin(), OWN_TICKET, "tech-legacy").getStatusCode().value());
        assertEquals(200, controller.assignTechnicalTicket(admin(), OWN_TICKET, TECH_ID).getStatusCode().value());
    }

    @Test
    void listTechniciansReturnsAdminOnesPlusLegacyKeepingIdAndNameShape() {
        when(technicianQueryService.handle(new GetTechniciansByAdminQuery(ADMIN_USER_ID))).thenReturn(List.of(
                new Technician(TECH_ID, "Maria Perez", TECH_USER_ID, ADMIN_USER_ID),
                new Technician("tech-legacy", "Carlos Seed")));

        var response = controller.getAllTechnicians(admin());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(new TechnicianResource(TECH_ID, "Maria Perez"),
                new TechnicianResource("tech-legacy", "Carlos Seed")), response.getBody());
    }

    @Test
    void createTechnicianThroughRosterEndpointSetsCallerAsOwningAdmin() {
        when(commandService.handle(any(CreateTechnician.class)))
                .thenReturn(Result.success(new Technician("tech-9", "Luis Roster", null, ADMIN_USER_ID)));

        var response = controller.createTechnician(admin(), new CreateTechnicianResource("Luis Roster"));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(new TechnicianResource("tech-9", "Luis Roster"), response.getBody());
        var captor = ArgumentCaptor.forClass(CreateTechnician.class);
        verify(commandService).handle(captor.capture());
        assertEquals(ADMIN_USER_ID, captor.getValue().adminUserId());
    }

    @Test
    void technicianEndpointsAreGuardedByTheRightRoles() {
        assertEquals("hasRole('TECHNICIAN')", preAuthorize("getTicketsAssignedToMe"));
        for (var method : List.of("completeMaintenance", "modifyTicketStatus", "registerMaintenanceCompletion",
                "getMaintenanceCompletionLog")) {
            assertEquals("hasAnyRole('ADMIN','TECHNICIAN')", preAuthorize(method));
        }
        for (var method : List.of("createTechnician", "getAllTechnicians", "assignTechnicalTicket",
                "getAllTickets", "createTechnicalTicket")) {
            assertEquals("hasRole('ADMIN')", preAuthorize(method));
        }
    }

    private void allowAdminOverTicket() {
        when(gymContextFacade.resolveGymIdForEquipment("eq-1")).thenReturn(Optional.of("gym-1"));
        when(gymContextFacade.isGymOwnedByAdmin("gym-1", ADMIN_USER_ID)).thenReturn(true);
    }

    private static String preAuthorize(String methodName) {
        var method = Arrays.stream(MaintenanceController.class.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        return method.getAnnotation(PreAuthorize.class).value();
    }

    private static TechnicalTicketPersistenceEntity ticketEntity(String ticketId, String technicianId) {
        var entity = new TechnicalTicketPersistenceEntity();
        entity.setTicketId(ticketId);
        entity.setMaintenanceId("m-1");
        entity.setEquipmentId("eq-1");
        entity.setTechnicianId(technicianId);
        entity.setDescription("d");
        entity.setPriority(TicketPriority.HIGH);
        entity.setType(TicketType.CORRECTIVE);
        entity.setTicketStatus("IN_PROGRESS");
        entity.setMaintenanceStatus("REQUESTED");
        return entity;
    }

    private static Authentication technician() {
        return new UsernamePasswordAuthenticationToken(TECH_EMAIL, "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_TECHNICIAN")));
    }

    private static Authentication admin() {
        return new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
