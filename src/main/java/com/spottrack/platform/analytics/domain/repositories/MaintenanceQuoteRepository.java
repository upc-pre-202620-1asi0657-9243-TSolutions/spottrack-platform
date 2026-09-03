package com.spottrack.platform.analytics.domain.repositories;

import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;
import java.util.List;
import java.util.Optional;

public interface MaintenanceQuoteRepository {
    MaintenanceQuote save(MaintenanceQuote maintenanceQuote);
    Optional<MaintenanceQuote> findByMaintenanceQuoteId(MaintenanceQuoteId maintenanceQuoteId);
    Optional<MaintenanceQuote> findById(Long id);
    List<MaintenanceQuote> findAll();
}
