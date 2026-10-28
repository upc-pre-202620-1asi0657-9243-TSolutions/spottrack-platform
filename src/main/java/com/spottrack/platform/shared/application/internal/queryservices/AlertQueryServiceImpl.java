package com.spottrack.platform.shared.application.internal.queryservices;

import com.spottrack.platform.shared.application.queryservices.AlertQueryService;
import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.queries.GetAlertsByAdminUserIdQuery;
import com.spottrack.platform.shared.domain.repositories.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;

    public AlertQueryServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public List<Alert> handle(GetAlertsByAdminUserIdQuery query) {
        Boolean resolved = switch (query.status()) {
            case PENDING -> false;
            case RESOLVED -> true;
            case ALL -> null;
        };
        return alertRepository.findAllNotClearedByAdminUserId(query.adminUserId(), resolved).stream()
                .filter(alert -> query.source() == null || alert.getSource() == query.source())
                .filter(alert -> query.severity() == null || alert.getSeverity() == query.severity())
                .toList();
    }
}
