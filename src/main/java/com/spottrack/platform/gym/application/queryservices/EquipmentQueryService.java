package com.spottrack.platform.gym.application.queryservices;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.queries.GetAvailableAlternativesQuery;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentStatus;
import com.spottrack.platform.gym.domain.model.queries.GetEquipments;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentById;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentByName;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import java.util.List;
import java.util.Optional;

public interface EquipmentQueryService {
   Optional<Equipment> handle(GetEquipmentById query);
    Optional<Equipment> handle(GetEquipmentByName query);
    Optional<Equipment>handle(GetEquipmentStatus query);
    Result<List<Equipment>, ApplicationError> handle(GetEquipments query);
    List<Equipment> handle(GetAvailableAlternativesQuery query);
}
