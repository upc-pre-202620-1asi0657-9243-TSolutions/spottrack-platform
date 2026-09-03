package com.spottrack.platform.iam.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.iam.domain.model.valueobjects.PendingRegistrationStatus;
import com.spottrack.platform.iam.infrastructure.persistence.jpa.entities.PendingRegistrationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingRegistrationPersistenceRepository
        extends JpaRepository<PendingRegistrationPersistenceEntity, UUID> {

    Optional<PendingRegistrationPersistenceEntity> findByEmail(String email);

    @Query("SELECT COUNT(p) > 0 FROM PendingRegistrationPersistenceEntity p " +
           "WHERE p.email = :email AND p.status = :status AND p.expiresAt > :now")
    boolean existsByEmailAndStatusAndExpiresAtAfter(
            @Param("email") String email,
            @Param("status") PendingRegistrationStatus status,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("DELETE FROM PendingRegistrationPersistenceEntity p " +
           "WHERE p.status = :status AND p.expiresAt <= :now")
    void deleteByStatusAndExpiresAtBefore(
            @Param("status") PendingRegistrationStatus status,
            @Param("now") LocalDateTime now
    );
}
