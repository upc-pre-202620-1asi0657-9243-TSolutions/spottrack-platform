package com.spottrack.platform.membership.application.internal.eventhandlers;

import com.spottrack.platform.membership.domain.model.events.PaymentFailedEvent;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipStatus;
import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentFailedEventHandler {

    private final MembershipRepository membershipRepository;

    PaymentFailedEventHandler(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @EventListener
    public void on(PaymentFailedEvent event) {
        log.info("Processing PaymentFailedEvent: paymentId={}, purpose={}",
                event.paymentId(), event.paymentPurpose());
        try {
            switch (event.paymentPurpose()) {
                case PLAN_UPGRADE -> log.info(
                        "Plan upgrade payment failed for membershipId={} (payment={}) — membership remains ACTIVE",
                        event.membershipId(), event.paymentId());
                case DEBT_RENEWAL -> log.info(
                        "Debt payment failed for membershipId={} (payment={}) — membership remains SUSPENDED",
                        event.membershipId(), event.paymentId());
                case NEW_MEMBERSHIP -> handleExistingUserPaymentFailure(event);
                case BUSINESS_REGISTRATION -> log.info(
                        "Business registration payment failed for pendingRegistrationId={} — no membership to suspend",
                        event.pendingRegistrationId());
                case RESUBSCRIPTION -> log.info(
                        "Resubscription payment failed for userId={} (payment={}) — user remains without active membership",
                        event.userId(), event.paymentId());
            }
        } catch (Exception e) {
            log.error("Error in PaymentFailedEventHandler for payment {}: {}",
                    event.paymentId(), e.getMessage(), e);
        }
    }

    private void handleExistingUserPaymentFailure(PaymentFailedEvent event) {
        var memberships = membershipRepository.findByClientId(event.userId());
        var activeMembership = memberships.stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .findFirst();

        if (activeMembership.isEmpty()) {
            log.info("No active membership found for userId {} (payment {}) — nothing to suspend",
                    event.userId(), event.paymentId());
            return;
        }

        var membership = activeMembership.get();
        membership.suspend();
        membershipRepository.save(membership);
        log.info("Membership {} suspended due to payment failure (userId={}, payment={})",
                membership.getMembershipId().uuid(), event.userId(), event.paymentId());
    }
}
