package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenanceLogPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceLogJpaRepository extends JpaRepository<MaintenanceLogPersistenceEntity, Long> {
    List<MaintenanceLogPersistenceEntity> findByTicketIdOrderByCompletedAtDesc(String ticketId);
}
