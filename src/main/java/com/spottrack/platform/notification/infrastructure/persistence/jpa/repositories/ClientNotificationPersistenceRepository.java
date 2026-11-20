package com.spottrack.platform.notification.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.notification.infrastructure.persistence.jpa.entities.ClientNotificationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientNotificationPersistenceRepository extends JpaRepository<ClientNotificationPersistenceEntity, Long> {
    List<ClientNotificationPersistenceEntity> findAllByClientIdOrderByCreatedAtDesc(Long clientId);

    List<ClientNotificationPersistenceEntity> findAllByClientIdAndReadAtIsNullOrderByCreatedAtDesc(Long clientId);
}
