package com.spottrack.platform.iam.domain.repositories;

import com.spottrack.platform.iam.domain.model.aggregates.PendingRegistration;

import java.util.Optional;
import java.util.UUID;

public interface PendingRegistrationRepository {
    PendingRegistration save(PendingRegistration pendingRegistration);
    Optional<PendingRegistration> findById(UUID registrationId);
    Optional<PendingRegistration> findByEmail(String email);
    boolean existsActivePendingByEmail(String email);
    void deleteExpiredPending();
}
