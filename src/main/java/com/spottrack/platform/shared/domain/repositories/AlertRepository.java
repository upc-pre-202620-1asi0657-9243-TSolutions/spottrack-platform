package com.spottrack.platform.shared.domain.repositories;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    Optional<Alert> findById(Long id);
    List<Alert> findAllByAdminUserId(Long adminUserId);
    Alert save(Alert alert);
}
