package com.spottrack.platform.membership.interfaces.rest.resources;

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

class BusinessRegistrationResourceTest {

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

    private static BusinessRegistrationResource resource(String phoneNumber, String companyPhone) {
        return new BusinessRegistrationResource(
                "owner@spotgym.pe", "S3cure!Pass", "Lucia", "Ramirez", phoneNumber, "74125836",
                "Spot Gym Miraflores", "20123456789", "S.A.C.", companyPhone, "contacto@spotgym.pe",
                "Av. Larco 123", "Lima", "Miraflores", "BASIC");
    }

    private static Set<String> invalidFields(BusinessRegistrationResource resource) {
        return validator.validate(resource).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void acceptsDigitOnlyPhoneNumbers() {
        assertTrue(validator.validate(resource("987654321", "5114445566")).isEmpty());
    }

    @Test
    void rejectsAnOwnerPhoneNumberWithPlusPrefix() {
        var violations = validator.validate(resource("+51987654321", "5114445566"));

        assertEquals(Set.of("phoneNumber"), invalidFields(resource("+51987654321", "5114445566")));
        assertTrue(violations.iterator().next().getMessage().contains("only digits"));
    }

    @Test
    void rejectsACompanyPhoneNumberWithPlusPrefix() {
        assertEquals(Set.of("companyPhone"), invalidFields(resource("987654321", "+5114445566")));
    }

    @Test
    void rejectsSpacesHyphensAndOutOfRangeLengths() {
        assertEquals(Set.of("phoneNumber"), invalidFields(resource("987 654 321", "5114445566")));
        assertEquals(Set.of("phoneNumber"), invalidFields(resource("987-654-321", "5114445566")));
        assertEquals(Set.of("phoneNumber"), invalidFields(resource("123456", "5114445566")));
        assertEquals(Set.of("companyPhone"), invalidFields(resource("987654321", "1234567890123456")));
    }

    @Test
    void stillRejectsBlankPhoneNumbers() {
        assertTrue(invalidFields(resource("", "5114445566")).contains("phoneNumber"));
        assertTrue(invalidFields(resource("987654321", " ")).contains("companyPhone"));
    }
}
