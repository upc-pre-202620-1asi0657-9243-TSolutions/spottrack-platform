package com.spottrack.platform.iam.infrastructure.scheduling;

import com.spottrack.platform.iam.domain.repositories.PendingRegistrationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PendingRegistrationCleanupScheduler {

    private final PendingRegistrationRepository pendingRegistrationRepository;

    public PendingRegistrationCleanupScheduler(PendingRegistrationRepository pendingRegistrationRepository) {
        this.pendingRegistrationRepository = pendingRegistrationRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredPendingRegistrations() {
        log.debug("Running expired pending registrations cleanup");
        pendingRegistrationRepository.deleteExpiredPending();
    }
}
