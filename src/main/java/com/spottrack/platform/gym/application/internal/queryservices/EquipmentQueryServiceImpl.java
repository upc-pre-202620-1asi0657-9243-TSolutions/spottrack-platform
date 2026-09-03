package com.spottrack.platform.gym.application.internal.queryservices;

import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.queries.GetAvailableAlternativesQuery;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentStatus;
import com.spottrack.platform.gym.domain.model.queries.GetEquipments;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentById;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentByName;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.EquipmentPersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.EquipmentPersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class EquipmentQueryServiceImpl implements EquipmentQueryService {
    EquipmentPersistenceRepository equipmentPersistenceRepository;

    public EquipmentQueryServiceImpl(EquipmentPersistenceRepository equipmentPersistenceRepository) {
        this.equipmentPersistenceRepository = equipmentPersistenceRepository;
    }

    @Override
    public Optional<Equipment> handle(GetEquipmentById query) {
        var equipment = equipmentPersistenceRepository.findByEquipmentId(query.id().uuid()).map(EquipmentPersistenceAssembler::toDomainFromPersistence);
        return equipment;
    }

    @Override
    public Optional<Equipment> handle(GetEquipmentByName query) {
        var equipment = equipmentPersistenceRepository.findByEquipmentName(query.equipmentName()).map(EquipmentPersistenceAssembler::toDomainFromPersistence);
        return equipment;
    }

    @Override
    public Optional<Equipment> handle(GetEquipmentStatus query) {
        return equipmentPersistenceRepository.findByEquipmentId(query.equipmentId().uuid()).map(EquipmentPersistenceAssembler::toDomainFromPersistence);
    }


    @Override
    public Result<List<Equipment>, ApplicationError> handle(GetEquipments query) {
        var persistenceEquipment = equipmentPersistenceRepository.findAll();
        var domain = persistenceEquipment.stream().map(EquipmentPersistenceAssembler::toDomainFromPersistence).toList();
        return Result.success(domain);
    }

    @Override
    public List<Equipment> handle(GetAvailableAlternativesQuery query) {
        return equipmentPersistenceRepository
                .findByEquipmentNameAndStatusAndEquipmentIdNot(query.equipmentName(), EquipmentStatus.AVAILABLE, query.excludeEquipmentId())
                .stream()
                .map(EquipmentPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
