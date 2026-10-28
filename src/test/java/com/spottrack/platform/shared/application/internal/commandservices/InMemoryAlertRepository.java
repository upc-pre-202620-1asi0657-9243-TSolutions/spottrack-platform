package com.spottrack.platform.shared.application.internal.commandservices;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.repositories.AlertRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Test double that applies the same not-cleared / resolved filtering as the JPA adapter. */
public class InMemoryAlertRepository implements AlertRepository {

    private final Map<Long, Alert> alerts = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Optional<Alert> findById(Long id) {
        return Optional.ofNullable(alerts.get(id));
    }

    @Override
    public List<Alert> findAllNotClearedByAdminUserId(Long adminUserId, Boolean resolved) {
        return alerts.values().stream()
                .filter(a -> a.getAdminUserId().equals(adminUserId))
                .filter(a -> !a.isCleared())
                .filter(a -> resolved == null || a.isResolved() == resolved)
                .toList();
    }

    @Override
    public Alert save(Alert alert) {
        if (alert.getId() == null) {
            alert.setId(nextId++);
        }
        alerts.put(alert.getId(), alert);
        return alert;
    }

    public List<Alert> all() {
        return new ArrayList<>(alerts.values());
    }
}
