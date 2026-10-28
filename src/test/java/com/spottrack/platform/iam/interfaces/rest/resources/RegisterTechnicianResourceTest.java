package com.spottrack.platform.iam.interfaces.rest.resources;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterTechnicianResourceTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static Set<String> invalidFields(RegisterTechnicianResource resource) {
        return validator.validate(resource).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void acceptsAValidPayload() {
        assertTrue(validator.validate(new RegisterTechnicianResource(
                "tecnico@gym.com", "secret123", "Maria", "Perez")).isEmpty());
    }

    @Test
    void rejectsMalformedEmail() {
        assertEquals(Set.of("email"), invalidFields(new RegisterTechnicianResource(
                "not-an-email", "secret123", "Maria", "Perez")));
    }

    @Test
    void rejectsShortAndOverlongPasswords() {
        assertEquals(Set.of("password"), invalidFields(new RegisterTechnicianResource(
                "tecnico@gym.com", "short", "Maria", "Perez")));
        assertEquals(Set.of("password"), invalidFields(new RegisterTechnicianResource(
                "tecnico@gym.com", "x".repeat(73), "Maria", "Perez")));
    }

    @Test
    void rejectsBlankOrMissingFields() {
        assertEquals(Set.of("email", "password", "firstName", "lastName"),
                invalidFields(new RegisterTechnicianResource(null, "", " ", null)));
    }

    @Test
    void violationMessagesArePlainReadableText() {
        var messages = validator.validate(new RegisterTechnicianResource("bad", "short", "", "")).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(messages.stream().noneMatch(m -> m.contains("{") || m.contains("validation.")));
    }
}
