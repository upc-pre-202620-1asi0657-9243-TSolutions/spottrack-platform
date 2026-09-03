package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.monitoring.domain.model.valueobjects.CameraSensorId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CameraSensorIdPersistenceConverter implements AttributeConverter<CameraSensorId, String> {
    @Override
    public String convertToDatabaseColumn(CameraSensorId attribute) {
        return attribute == null ? null : attribute.uuid();
    }

    @Override
    public CameraSensorId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new CameraSensorId(dbData);
    }
}
