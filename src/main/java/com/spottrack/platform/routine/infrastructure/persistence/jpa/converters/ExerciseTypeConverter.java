package com.spottrack.platform.routine.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ExerciseTypeConverter implements AttributeConverter<ExerciseType, String> {

    @Override
    public String convertToDatabaseColumn(ExerciseType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ExerciseType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ExerciseType.valueOf(dbData);
    }
}
