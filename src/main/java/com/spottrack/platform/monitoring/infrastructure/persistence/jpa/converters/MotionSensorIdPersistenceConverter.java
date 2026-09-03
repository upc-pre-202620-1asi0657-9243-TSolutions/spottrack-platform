package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MotionSensorIdPersistenceConverter implements AttributeConverter<MotionSensorId, String> {
    @Override
    public String convertToDatabaseColumn(MotionSensorId attribute) {
        return attribute == null ? null : attribute.uuid();
    }

    @Override
    public MotionSensorId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new MotionSensorId(dbData);
    }
}
