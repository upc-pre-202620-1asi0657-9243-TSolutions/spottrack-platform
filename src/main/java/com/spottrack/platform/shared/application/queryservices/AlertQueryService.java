package com.spottrack.platform.shared.application.queryservices;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.queries.GetAlertsByAdminUserIdQuery;

import java.util.List;

public interface AlertQueryService {
    List<Alert> handle(GetAlertsByAdminUserIdQuery query);
}
