package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.monitoring.domain.model.valueobjects.AnomalyId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.AnomalyPersistenceEntity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AnomalyIdPersistenceConverter implements AttributeConverter<AnomalyId, String> {

    @Override
    public String convertToDatabaseColumn(AnomalyId attribute) {
        return attribute == null ? null : attribute.uuid();
    }

    @Override
    public AnomalyId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new AnomalyId(dbData);
    }
}
