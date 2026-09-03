package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.application.queryservices.MaintenanceQuoteQueryService;
import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.domain.model.queries.GetAllMaintenanceQuotesQuery;
import com.spottrack.platform.analytics.domain.repositories.MaintenanceQuoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceQuoteQueryServiceImpl implements MaintenanceQuoteQueryService {

    private final MaintenanceQuoteRepository maintenanceQuoteRepository;

    public MaintenanceQuoteQueryServiceImpl(MaintenanceQuoteRepository maintenanceQuoteRepository) {
        this.maintenanceQuoteRepository = maintenanceQuoteRepository;
    }

    @Override
    public List<MaintenanceQuote> handle(GetAllMaintenanceQuotesQuery query) {
        return maintenanceQuoteRepository.findAll();
    }
}
