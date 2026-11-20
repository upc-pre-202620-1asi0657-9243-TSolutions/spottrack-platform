package com.spottrack.platform.notification.domain.repositories;

import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchOrigin;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchStatus;

import java.util.List;
import java.util.Optional;

public interface EquipmentWatchRepository {
    Optional<EquipmentWatch> findActiveByClientIdAndEquipmentIdAndOrigin(Long clientId, String equipmentId, WatchOrigin origin);

    List<EquipmentWatch> findAllByClientIdAndStatus(Long clientId, WatchStatus status);

    List<EquipmentWatch> findAllByEquipmentIdAndStatus(String equipmentId, WatchStatus status);

    EquipmentWatch save(EquipmentWatch watch);
}
