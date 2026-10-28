package com.spottrack.platform.shared.domain.model.queries;

import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertStatusFilter;

/** Cleared alerts are never returned. Null {@code source} and {@code severity} mean "any". */
public record GetAlertsByAdminUserIdQuery(Long adminUserId, AlertStatusFilter status, AlertSource source,
                                          AlertSeverity severity) {
    public GetAlertsByAdminUserIdQuery {
        if (status == null) {
            status = AlertStatusFilter.PENDING;
        }
    }
}
