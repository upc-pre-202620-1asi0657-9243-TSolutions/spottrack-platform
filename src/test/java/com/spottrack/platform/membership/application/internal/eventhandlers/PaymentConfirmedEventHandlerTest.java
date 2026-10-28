package com.spottrack.platform.membership.application.internal.eventhandlers;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.events.PaymentConfirmedEvent;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipStatus;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentPurpose;
import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentConfirmedEventHandlerTest {

    private static final Long USER_ID = 42L;
    private static final UUID PENDING_ID = UUID.randomUUID();

    private final List<Membership> storedMemberships = new ArrayList<>();
    private MembershipRepository membershipRepository;
    private IamContextFacade iamContextFacade;
    private ProfilesContextFacade profilesContextFacade;
    private GymContextFacade gymContextFacade;
    private PaymentConfirmedEventHandler handler;

    @BeforeEach
    void setUp() {
        membershipRepository = mock(MembershipRepository.class);
        iamContextFacade = mock(IamContextFacade.class);
        profilesContextFacade = mock(ProfilesContextFacade.class);
        gymContextFacade = mock(GymContextFacade.class);

        when(membershipRepository.findByClientId(anyLong())).thenAnswer(inv -> List.copyOf(storedMemberships));
        when(membershipRepository.save(any(Membership.class))).thenAnswer(inv -> {
            Membership membership = inv.getArgument(0);
            if (!storedMemberships.contains(membership)) {
                storedMemberships.add(membership);
            }
            return membership;
        });
        when(iamContextFacade.consumePendingRegistration(PENDING_ID)).thenReturn(USER_ID);
        when(iamContextFacade.fetchPendingRegistrationEmail(PENDING_ID)).thenReturn("owner@spotgym.pe");

        handler = new PaymentConfirmedEventHandler(
                membershipRepository, iamContextFacade, profilesContextFacade, gymContextFacade);
    }

    private PaymentConfirmedEvent businessRegistrationEvent() {
        return new PaymentConfirmedEvent(
                1L, UUID.randomUUID(), null, PENDING_ID, null,
                MembershipTier.BASIC, new Money(BigDecimal.valueOf(69), "USD"),
                "cs_test_123", PaymentPurpose.BUSINESS_REGISTRATION);
    }

    @Test
    void createsGymNamedAfterTheCompanyOnceTheBusinessPaymentIsConfirmed() {
        when(iamContextFacade.fetchPendingRegistrationCompanyName(PENDING_ID)).thenReturn("Spot Gym Miraflores");

        handler.on(businessRegistrationEvent());

        verify(gymContextFacade).provisionGym(USER_ID, "Spot Gym Miraflores");
        assertEquals(1, storedMemberships.size());
        assertEquals(MembershipStatus.ACTIVE, storedMemberships.get(0).getStatus());
    }

    @Test
    void reprocessingTheSamePaymentKeepsASingleMembershipAndAsksForTheSameGym() {
        when(iamContextFacade.fetchPendingRegistrationCompanyName(PENDING_ID)).thenReturn("Spot Gym Miraflores");
        var event = businessRegistrationEvent();

        handler.on(event);
        handler.on(event);

        assertEquals(1, storedMemberships.size());
        verify(gymContextFacade, times(2)).provisionGym(USER_ID, "Spot Gym Miraflores");
    }

    @Test
    void skipsGymCreationButKeepsTheMembershipWhenTheCompanyNameIsBlank() {
        when(iamContextFacade.fetchPendingRegistrationCompanyName(PENDING_ID)).thenReturn("  ");

        handler.on(businessRegistrationEvent());

        verify(gymContextFacade, never()).provisionGym(anyLong(), anyString());
        assertTrue(storedMemberships.stream().anyMatch(m -> m.getStatus() == MembershipStatus.ACTIVE));
    }
}
