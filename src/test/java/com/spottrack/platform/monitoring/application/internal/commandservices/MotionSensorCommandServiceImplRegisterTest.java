package com.spottrack.platform.monitoring.application.internal.commandservices;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.monitoring.application.ports.PayloadProfileCatalog;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MotionSensorCommandServiceImplRegisterTest {

    private static final String EQUIPMENT_ID = "eq-1";

    private MotionSensorRepository motionSensorRepository;
    private GymContextFacade gymContextFacade;
    private PayloadProfileCatalog payloadProfileCatalog;
    private MotionSensorCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        motionSensorRepository = mock(MotionSensorRepository.class);
        gymContextFacade = mock(GymContextFacade.class);
        payloadProfileCatalog = mock(PayloadProfileCatalog.class);
        service = new MotionSensorCommandServiceImpl(motionSensorRepository, gymContextFacade, payloadProfileCatalog);

        when(gymContextFacade.findEquipmentById(EQUIPMENT_ID)).thenReturn(Optional.of(mock(Equipment.class)));
        when(motionSensorRepository.existsByEquipmentId(new EquipmentId(EQUIPMENT_ID))).thenReturn(false);
        when(motionSensorRepository.save(any(MotionSensor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(payloadProfileCatalog.isSupported("alt")).thenReturn(true);
    }

    @Test
    void registrationWithSupportedProfileStoresIt() {
        var result = service.handle(new RegisterMotionSensorCommand(EQUIPMENT_ID, "alt"));

        assertInstanceOf(Result.Success.class, result);
        assertEquals("alt", savedSensor().getPayloadProfile());
        assertEquals("alt", result.toOptional().orElseThrow().getPayloadProfile());
    }

    @Test
    void registrationWithoutProfileStoresNull() {
        var result = service.handle(new RegisterMotionSensorCommand(EQUIPMENT_ID, null));

        assertInstanceOf(Result.Success.class, result);
        assertNull(savedSensor().getPayloadProfile());
        verify(payloadProfileCatalog, never()).isSupported(any());
    }

    @Test
    void registrationWithBlankProfileStoresNull() {
        when(payloadProfileCatalog.isSupported("  ")).thenReturn(true);

        var result = service.handle(new RegisterMotionSensorCommand(EQUIPMENT_ID, "  "));

        assertInstanceOf(Result.Success.class, result);
        assertNull(savedSensor().getPayloadProfile());
    }

    @Test
    void registrationWithUnknownProfileIsAValidationErrorAndIsNotPersisted() {
        when(payloadProfileCatalog.isSupported("nope")).thenReturn(false);

        var result = service.handle(new RegisterMotionSensorCommand(EQUIPMENT_ID, "nope"));

        assertEquals("VALIDATION_ERROR", errorOf(result).code());
        verify(motionSensorRepository, never()).save(any(MotionSensor.class));
    }

    @Test
    void duplicatedSensorForTheEquipmentIsStillAConflict() {
        when(motionSensorRepository.existsByEquipmentId(new EquipmentId(EQUIPMENT_ID))).thenReturn(true);

        var result = service.handle(new RegisterMotionSensorCommand(EQUIPMENT_ID, "alt"));

        assertEquals("MOTIONSENSOR_CONFLICT", errorOf(result).code());
        verify(motionSensorRepository, never()).save(any(MotionSensor.class));
    }

    @Test
    void unknownEquipmentIsStillNotFound() {
        when(gymContextFacade.findEquipmentById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        var result = service.handle(new RegisterMotionSensorCommand(EQUIPMENT_ID, null));

        assertEquals("EQUIPMENT_NOT_FOUND", errorOf(result).code());
        verify(motionSensorRepository, never()).save(any(MotionSensor.class));
    }

    private MotionSensor savedSensor() {
        var captor = ArgumentCaptor.forClass(MotionSensor.class);
        verify(motionSensorRepository).save(captor.capture());
        return captor.getValue();
    }

    private static ApplicationError errorOf(Result<MotionSensor, ApplicationError> result) {
        return assertInstanceOf(Result.Failure.class, result) instanceof Result.Failure<?, ?> failure
                ? (ApplicationError) failure.error()
                : null;
    }
}
