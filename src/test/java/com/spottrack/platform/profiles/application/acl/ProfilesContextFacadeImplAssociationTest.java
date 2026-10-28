package com.spottrack.platform.profiles.application.acl;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.profiles.application.commandservices.AdminCommandService;
import com.spottrack.platform.profiles.application.commandservices.BusinessProfileCommandService;
import com.spottrack.platform.profiles.application.queryservices.AdminQueryService;
import com.spottrack.platform.profiles.application.queryservices.ClientQueryService;
import com.spottrack.platform.profiles.domain.model.aggregates.Client;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByIdQuery;
import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PersonInfo;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories.ClientGymAssociationPersistenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfilesContextFacadeImplAssociationTest {

    private static final Long CLIENT_ID = 7L;
    private static final String GYM_ID = "gym-san-isidro";
    private static final String DNI = "12345678";

    private ClientQueryService clientQueryService;
    private ClientGymAssociationPersistenceRepository associationRepository;
    private GymContextFacade gymContextFacade;
    private ProfilesContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        clientQueryService = mock(ClientQueryService.class);
        associationRepository = mock(ClientGymAssociationPersistenceRepository.class);
        gymContextFacade = mock(GymContextFacade.class);
        facade = new ProfilesContextFacadeImpl(
                clientQueryService,
                mock(AdminQueryService.class),
                mock(AdminCommandService.class),
                mock(BusinessProfileCommandService.class),
                associationRepository,
                gymContextFacade);
    }

    @Test
    void acceptsAnAssociatedGymWithAWhitelistedDniEvenWhenItIsNotTheActiveOne() {
        when(associationRepository.existsByClientIdAndGymId(CLIENT_ID, GYM_ID)).thenReturn(true);
        when(clientQueryService.handle(any(GetClientByIdQuery.class))).thenReturn(Optional.of(completeClient()));
        when(gymContextFacade.isDniWhitelistedForGym(GYM_ID, DNI)).thenReturn(true);

        assertTrue(facade.hasAssociationWithGym(CLIENT_ID, GYM_ID));
        verify(associationRepository, never()).findByClientIdAndActiveTrue(any());
    }

    @Test
    void rejectsAGymTheClientIsNotAssociatedWith() {
        when(associationRepository.existsByClientIdAndGymId(CLIENT_ID, GYM_ID)).thenReturn(false);

        assertFalse(facade.hasAssociationWithGym(CLIENT_ID, GYM_ID));
        verify(gymContextFacade, never()).isDniWhitelistedForGym(any(), any());
    }

    @Test
    void rejectsAnAssociatedGymWhenTheDniWasRemovedFromItsWhitelist() {
        when(associationRepository.existsByClientIdAndGymId(CLIENT_ID, GYM_ID)).thenReturn(true);
        when(clientQueryService.handle(any(GetClientByIdQuery.class))).thenReturn(Optional.of(completeClient()));
        when(gymContextFacade.isDniWhitelistedForGym(GYM_ID, DNI)).thenReturn(false);

        assertFalse(facade.hasAssociationWithGym(CLIENT_ID, GYM_ID));
    }

    @Test
    void rejectsWhenTheClientProfileIsIncomplete() {
        when(associationRepository.existsByClientIdAndGymId(CLIENT_ID, GYM_ID)).thenReturn(true);
        var incomplete = new Client(CLIENT_ID, 1L, null, new EmailAddress("ana@mail.com"));
        when(clientQueryService.handle(any(GetClientByIdQuery.class))).thenReturn(Optional.of(incomplete));

        assertFalse(facade.hasAssociationWithGym(CLIENT_ID, GYM_ID));
        verify(gymContextFacade, never()).isDniWhitelistedForGym(any(), any());
    }

    @Test
    void rejectsWhenTheClientDoesNotExist() {
        when(associationRepository.existsByClientIdAndGymId(CLIENT_ID, GYM_ID)).thenReturn(true);
        when(clientQueryService.handle(any(GetClientByIdQuery.class))).thenReturn(Optional.empty());

        assertFalse(facade.hasAssociationWithGym(CLIENT_ID, GYM_ID));
    }

    private static Client completeClient() {
        var person = new PersonInfo("Ana", "Torres", new PhoneNumber("987654321"), new Dni(DNI));
        return new Client(CLIENT_ID, 1L, person, new EmailAddress("ana@mail.com"));
    }
}
