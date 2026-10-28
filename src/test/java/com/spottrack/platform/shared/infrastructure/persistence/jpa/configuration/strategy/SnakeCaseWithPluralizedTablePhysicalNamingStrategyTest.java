package com.spottrack.platform.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeCaseWithPluralizedTablePhysicalNamingStrategyTest {

    private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy =
            new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

    @Test
    void quotedColumnNameKeepsQuotedFlag() {
        var result = strategy.toPhysicalColumnName(Identifier.toIdentifier("sets", true), null);

        assertEquals("sets", result.getText());
        assertTrue(result.isQuoted());
    }

    @Test
    void quotedTableNameKeepsQuotedFlag() {
        var result = strategy.toPhysicalTableName(Identifier.toIdentifier("order", true), null);

        assertEquals("orders", result.getText());
        assertTrue(result.isQuoted());
    }

    @Test
    void camelCaseColumnNameIsConvertedToSnakeCase() {
        var result = strategy.toPhysicalColumnName(Identifier.toIdentifier("exerciseName"), null);

        assertEquals("exercise_name", result.getText());
        assertFalse(result.isQuoted());
    }

    @Test
    void camelCaseTableNameIsConvertedToPluralSnakeCase() {
        var result = strategy.toPhysicalTableName(Identifier.toIdentifier("exerciseBlock"), null);

        assertEquals("exercise_blocks", result.getText());
        assertFalse(result.isQuoted());
    }
}
