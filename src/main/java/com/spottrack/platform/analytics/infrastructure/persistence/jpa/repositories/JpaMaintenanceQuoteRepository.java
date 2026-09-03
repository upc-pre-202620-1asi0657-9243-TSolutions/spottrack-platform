package com.spottrack.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.entities.MaintenanceQuotePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaMaintenanceQuoteRepository extends JpaRepository<MaintenanceQuotePersistenceEntity, Long> {
    Optional<MaintenanceQuotePersistenceEntity> findByMaintenanceQuoteId(MaintenanceQuoteId maintenanceQuoteId);
}
