package com.spottrack.platform.gym.interfaces.rest.controllers;

import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.domain.model.entities.Zone;
import com.spottrack.platform.gym.domain.model.queries.GetBranchesByGymIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetGymById;
import com.spottrack.platform.gym.domain.model.queries.GetZonesByGymIdQuery;
import com.spottrack.platform.gym.domain.model.valueobjects.BranchId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GymControllerReadAccessTest {

    private static final String GYM_ID = "gym-san-isidro";
    private static final String CLIENT_EMAIL = "ana@mail.com";
    private static final String ADMIN_EMAIL = "admin@mail.com";
    private static final Long CLIENT_ID = 7L;
    private static final Long ADMIN_USER_ID = 42L;

    private GymQueryService gymQueryService;
    private IamContextFacade iamContextFacade;
    private ProfilesContextFacade profilesContextFacade;
    private GymContextFacade gymContextFacade;
    private GymController controller;

    @BeforeEach
    void setUp() {
        gymQueryService = mock(GymQueryService.class);
        iamContextFacade = mock(IamContextFacade.class);
        profilesContextFacade = mock(ProfilesContextFacade.class);
        gymContextFacade = mock(GymContextFacade.class);
        controller = new GymController(mock(GymCommandService.class), gymQueryService,
                iamContextFacade, profilesContextFacade, gymContextFacade);
    }

    @Test
    void clientReadsBranchesZonesAndEquipmentsOfAnAssociatedGymThatIsNotActive() {
        when(profilesContextFacade.fetchClientIdByEmail(CLIENT_EMAIL)).thenReturn(CLIENT_ID);
        when(profilesContextFacade.hasAssociationWithGym(CLIENT_ID, GYM_ID)).thenReturn(true);
        when(profilesContextFacade.hasActiveAssociationWithGym(CLIENT_ID, GYM_ID)).thenReturn(false);
        when(gymQueryService.handle(any(GetBranchesByGymIdQuery.class)))
                .thenReturn(List.of(new Branch("b-1", GYM_ID, "San Isidro Centro", "Av. Javier Prado 100")));
        when(gymQueryService.handle(any(GetZonesByGymIdQuery.class)))
                .thenReturn(List.of(new Zone("z-1", "Cardio", 30, new BranchId("b-1"))));
        when(gymContextFacade.findEquipmentsByGymId(GYM_ID)).thenReturn(List.of(equipment()));
        var auth = client();

        var branches = controller.getBranchesByGymId(auth, GYM_ID);
        var zones = controller.getZonesByGymId(auth, GYM_ID);
        var equipments = controller.getEquipmentsByGymId(auth, GYM_ID);

        assertEquals(200, branches.getStatusCode().value());
        assertEquals(1, ((List<?>) branches.getBody()).size());
        assertEquals(200, zones.getStatusCode().value());
        assertEquals(1, ((List<?>) zones.getBody()).size());
        assertEquals(200, equipments.getStatusCode().value());
        assertEquals(1, ((List<?>) equipments.getBody()).size());
    }

    @Test
    void clientGets403ForAGymItIsNotAssociatedWith() {
        when(profilesContextFacade.fetchClientIdByEmail(CLIENT_EMAIL)).thenReturn(CLIENT_ID);
        when(profilesContextFacade.hasAssociationWithGym(CLIENT_ID, GYM_ID)).thenReturn(false);
        var auth = client();

        for (var response : List.of(controller.getBranchesByGymId(auth, GYM_ID),
                controller.getZonesByGymId(auth, GYM_ID),
                controller.getEquipmentsByGymId(auth, GYM_ID))) {
            assertEquals(403, response.getStatusCode().value());
            assertEquals("FORBIDDEN", assertInstanceOf(ErrorResource.class, response.getBody()).code());
        }
        verify(gymQueryService, never()).handle(any(GetBranchesByGymIdQuery.class));
        verify(gymContextFacade, never()).findEquipmentsByGymId(any());
    }

    @Test
    void clientGets403EvenWhenTheGymDoesNotExist() {
        when(profilesContextFacade.fetchClientIdByEmail(CLIENT_EMAIL)).thenReturn(CLIENT_ID);
        when(profilesContextFacade.hasAssociationWithGym(CLIENT_ID, "ghost")).thenReturn(false);

        var response = controller.getZonesByGymId(client(), "ghost");

        assertEquals(403, response.getStatusCode().value());
        verify(gymQueryService, never()).handle(any(GetGymById.class));
    }

    @Test
    void unknownClientProfileGets404() {
        when(profilesContextFacade.fetchClientIdByEmail(CLIENT_EMAIL)).thenReturn(0L);

        var response = controller.getBranchesByGymId(client(), GYM_ID);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("CLIENT_NOT_FOUND", assertInstanceOf(ErrorResource.class, response.getBody()).code());
    }

    @Test
    void adminOwnerStillReadsItsGymWithoutTheClientAssociationCheck() {
        when(iamContextFacade.fetchUserIdByUsername(ADMIN_EMAIL)).thenReturn(Optional.of(ADMIN_USER_ID));
        when(gymQueryService.handle(any(GetGymById.class)))
                .thenReturn(Optional.of(new Gym(new GymId(GYM_ID), "Spot Gym", ADMIN_USER_ID)));
        when(gymQueryService.handle(any(GetZonesByGymIdQuery.class))).thenReturn(List.of());

        var response = controller.getZonesByGymId(admin(), GYM_ID);

        assertEquals(200, response.getStatusCode().value());
        verify(profilesContextFacade, never()).hasAssociationWithGym(any(), any());
    }

    @Test
    void adminThatDoesNotOwnTheGymGets403AndAMissingGymGets404() {
        when(iamContextFacade.fetchUserIdByUsername(ADMIN_EMAIL)).thenReturn(Optional.of(99L));
        when(gymQueryService.handle(any(GetGymById.class)))
                .thenReturn(Optional.of(new Gym(new GymId(GYM_ID), "Spot Gym", ADMIN_USER_ID)))
                .thenReturn(Optional.empty());

        assertEquals(403, controller.getBranchesByGymId(admin(), GYM_ID).getStatusCode().value());
        assertEquals(404, controller.getBranchesByGymId(admin(), GYM_ID).getStatusCode().value());
    }

    private static Equipment equipment() {
        return new Equipment(EquipmentStatus.OCCUPIED, "Treadmill", "T-1", new ManufacturerId("m-1"),
                BigDecimal.TEN, "USD", new ZoneId("z-1"));
    }

    private static Authentication client() {
        return new UsernamePasswordAuthenticationToken(CLIENT_EMAIL, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }

    private static Authentication admin() {
        return new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
