package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EquipmentIdPersistenceConverter implements AttributeConverter<EquipmentId, String> {
    @Override
    public String convertToDatabaseColumn(EquipmentId attribute) {
        return attribute == null ? null : attribute.uuid();
    }

    @Override
    public EquipmentId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new EquipmentId(dbData);
    }
}
