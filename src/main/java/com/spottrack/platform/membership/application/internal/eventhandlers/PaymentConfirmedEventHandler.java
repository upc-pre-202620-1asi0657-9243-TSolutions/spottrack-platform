package com.spottrack.platform.membership.application.internal.eventhandlers;

import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.commands.CreateMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.RenewMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.UpgradeMembershipPlanCommand;
import com.spottrack.platform.membership.domain.model.events.PaymentConfirmedEvent;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipStatus;
import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class PaymentConfirmedEventHandler {

    private final MembershipRepository membershipRepository;
    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;

    PaymentConfirmedEventHandler(MembershipRepository membershipRepository,
                                 IamContextFacade iamContextFacade,
                                 ProfilesContextFacade profilesContextFacade) {
        this.membershipRepository = membershipRepository;
        this.iamContextFacade = iamContextFacade;
        this.profilesContextFacade = profilesContextFacade;
    }

    @EventListener
    public void on(PaymentConfirmedEvent event) {
        log.info("Processing PaymentConfirmedEvent: paymentId={}, purpose={}, tier={}, amount={}",
                event.paymentId(), event.paymentPurpose(), event.membershipTier(), event.amount());
        try {
            switch (event.paymentPurpose()) {
                case PLAN_UPGRADE -> handlePlanUpgradePayment(event);
                case DEBT_RENEWAL -> handleDebtPayment(event);
                case NEW_MEMBERSHIP -> handleExistingUserPayment(event);
                case BUSINESS_REGISTRATION -> handleBusinessRegistrationPayment(event);
                case RESUBSCRIPTION -> handleResubscriptionPayment(event);
            }
        } catch (Exception e) {
            log.error("Error in PaymentConfirmedEventHandler for payment {} : {}",
                    event.paymentId(), e.getMessage(), e);
        }
    }

    private void handleDebtPayment(PaymentConfirmedEvent event) {
        var membership = membershipRepository.findByMembershipId(new MembershipId(event.membershipId()));
        if (membership.isEmpty()) {
            log.error("Debt payment confirmed for unknown membershipId={} (payment {}) — skipping",
                    event.membershipId(), event.paymentId());
            return;
        }
        var m = membership.get();
        if (m.getStatus() == MembershipStatus.ACTIVE) {
            log.info("Membership {} already ACTIVE — skipping debt renewal (idempotent, payment {})",
                    event.membershipId(), event.paymentId());
            return;
        }
        if (m.getStatus() != MembershipStatus.SUSPENDED) {
            log.warn("Membership {} is in status {} — cannot renew via debt payment (payment {}). Manual review required.",
                    event.membershipId(), m.getStatus(), event.paymentId());
            return;
        }
        var today = LocalDate.now();
        var command = new RenewMembershipCommand(new MembershipId(event.membershipId()), today, today.plusDays(30));
        m.renew(command);
        membershipRepository.save(m);
        log.info("Membership {} renewed after debt payment (clientId={}, payment {})",
                event.membershipId(), m.getClientId(), event.paymentId());
    }

    private void handlePlanUpgradePayment(PaymentConfirmedEvent event) {
        var membership = membershipRepository.findByMembershipId(new MembershipId(event.membershipId()));
        if (membership.isEmpty()) {
            log.error("Plan upgrade payment confirmed for unknown membershipId={} (payment {}) — skipping",
                    event.membershipId(), event.paymentId());
            return;
        }
        var m = membership.get();
        if (m.getMembershipTier() == event.membershipTier()) {
            log.info("Membership {} already at tier {} — upgrade payment idempotent (payment {})",
                    event.membershipId(), event.membershipTier(), event.paymentId());
            return;
        }
        if (m.getStatus() != MembershipStatus.ACTIVE) {
            log.warn("Membership {} is in status {} — cannot apply plan upgrade (payment {}). Manual review required.",
                    event.membershipId(), m.getStatus(), event.paymentId());
            return;
        }
        var command = new UpgradeMembershipPlanCommand(new MembershipId(event.membershipId()), event.membershipTier());
        m.upgradePlan(command);
        membershipRepository.save(m);
        log.info("Membership {} upgraded to tier {} (clientId={}, payment {})",
                event.membershipId(), event.membershipTier(), m.getClientId(), event.paymentId());
    }

    private void handleExistingUserPayment(PaymentConfirmedEvent event) {
        createAndActivateMembership(event.userId(), event);
    }

    private void handleResubscriptionPayment(PaymentConfirmedEvent event) {
        createAndActivateMembership(event.userId(), event);
    }

    private void handleBusinessRegistrationPayment(PaymentConfirmedEvent event) {
        var pendingId = event.pendingRegistrationId();

        var userId = iamContextFacade.consumePendingRegistration(pendingId);
        if (userId == 0L) {
            log.error("PendingRegistration {} not found, expired, or failed to provision — cannot process payment {}",
                    pendingId, event.paymentId());
            return;
        }

        log.info("Provisioning account for pendingRegistrationId={}, resolved userId={}", pendingId, userId);

        var email          = iamContextFacade.fetchPendingRegistrationEmail(pendingId);
        var firstName      = iamContextFacade.fetchPendingRegistrationFirstName(pendingId);
        var lastName       = iamContextFacade.fetchPendingRegistrationLastName(pendingId);
        var phoneNumber    = iamContextFacade.fetchPendingRegistrationPhoneNumber(pendingId);
        var dni            = iamContextFacade.fetchPendingRegistrationDni(pendingId);
        var companyName    = iamContextFacade.fetchPendingRegistrationCompanyName(pendingId);
        var ruc            = iamContextFacade.fetchPendingRegistrationRuc(pendingId);
        var legalStructure = iamContextFacade.fetchPendingRegistrationLegalStructure(pendingId);
        var companyPhone   = iamContextFacade.fetchPendingRegistrationCompanyPhone(pendingId);
        var companyEmail   = iamContextFacade.fetchPendingRegistrationCompanyEmail(pendingId);
        var streetAddress  = iamContextFacade.fetchPendingRegistrationStreetAddress(pendingId);
        var city           = iamContextFacade.fetchPendingRegistrationCity(pendingId);
        var district       = iamContextFacade.fetchPendingRegistrationDistrict(pendingId);

        profilesContextFacade.provisionAdminProfile(userId, email, firstName, lastName, phoneNumber, dni);

        profilesContextFacade.provisionBusinessProfile(
                userId,
                companyName, ruc, legalStructure,
                companyPhone, companyEmail,
                streetAddress, city, district
        );

        createAndActivateMembership(userId, event);
    }

    private void createAndActivateMembership(Long userId, PaymentConfirmedEvent event) {
        var existing = membershipRepository.findByClientId(userId);
        boolean hasActiveMembership = existing.stream()
                .anyMatch(m -> m.getStatus() == MembershipStatus.ACTIVE);

        if (hasActiveMembership) {
            log.info("Active membership already exists for userId {}, skipping creation (payment {})",
                    userId, event.paymentId());
            return;
        }

        var startDate = LocalDate.now();
        var endDate = startDate.plusDays(30);

        var command = new CreateMembershipCommand(
                userId,
                event.membershipTier(),
                event.amount(),
                startDate,
                endDate
        );

        Membership membership = membershipRepository.save(new Membership(command));
        log.info("Membership {} created for userId {}, activating", membership.getMembershipId(), userId);

        membership.activate();
        membershipRepository.save(membership);

        log.info("Membership {} is now ACTIVE for userId {} (payment {})",
                membership.getMembershipId(), userId, event.paymentId());
    }
}
