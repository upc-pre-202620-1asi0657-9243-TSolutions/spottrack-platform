package com.spottrack.platform.shared.domain.repositories;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    Optional<Alert> findById(Long id);

    /** Alerts of the admin that have not been cleared; {@code resolved} null returns both pending and resolved. */
    List<Alert> findAllNotClearedByAdminUserId(Long adminUserId, Boolean resolved);

    Alert save(Alert alert);
}
