package com.spottrack.platform.membership.infrastructure.scheduling;

import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class MembershipExpiryScheduler {

    private final MembershipRepository membershipRepository;

    public MembershipExpiryScheduler(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 3_600_000)
    public void expireActiveMemberships() {
        var today = LocalDate.now();

        var pendingCancellation = membershipRepository.findPendingCancellationExpiredBefore(today);
        log.debug("Membership expiry check: found {} membership(s) pending cancellation past endDate", pendingCancellation.size());
        pendingCancellation.forEach(membership -> {
            membership.completeCancellation();
            membershipRepository.save(membership);
            log.info("Membership {} cancelled at period end (clientId={})", membership.getMembershipId().uuid(), membership.getClientId());
        });

        var expired = membershipRepository.findActiveExpiredBefore(today);
        log.debug("Membership expiry check: found {} active membership(s) past endDate", expired.size());
        expired.forEach(membership -> {
            if (membership.getPendingDowngradeTier() != null) {
                membership.applyDowngrade();
                membershipRepository.save(membership);
                log.info("Membership {} downgraded to tier {} at period end (clientId={})",
                        membership.getMembershipId().uuid(), membership.getMembershipTier(), membership.getClientId());
            } else {
                membership.expire();
                membershipRepository.save(membership);
                log.info("Membership {} expired (clientId={})", membership.getMembershipId().uuid(), membership.getClientId());
            }
        });
    }
}
