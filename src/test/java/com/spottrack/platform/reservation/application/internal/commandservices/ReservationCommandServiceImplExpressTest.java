package com.spottrack.platform.reservation.application.internal.commandservices;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.commands.ExpireReservation;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.commands.StartReservationTimer;
import com.spottrack.platform.reservation.domain.model.valueobjects.ClientId;
import com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.reservation.domain.repositories.ReservationRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.Time;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationCommandServiceImplExpressTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 27, 18, 30, 0);
    private static final Long CLIENT_ID = 5L;
    private static final String EQUIPMENT_ID = "eq-1";
    private static final String GYM_ID = "gym-1";

    private ReservationRepository reservationRepository;
    private GymContextFacade gymContextFacade;
    private ProfilesContextFacade profilesContextFacade;
    private Equipment equipment;
    private ReservationCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        gymContextFacade = mock(GymContextFacade.class);
        profilesContextFacade = mock(ProfilesContextFacade.class);
        equipment = mock(Equipment.class);
        var clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        service = new ReservationCommandServiceImpl(reservationRepository, gymContextFacade, profilesContextFacade, clock, 10);

        when(equipment.getStatus()).thenReturn(EquipmentStatus.AVAILABLE);
        when(gymContextFacade.findEquipmentById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment));
        when(gymContextFacade.resolveGymIdForEquipment(EQUIPMENT_ID)).thenReturn(Optional.of(GYM_ID));
        when(profilesContextFacade.fetchActiveGymIdByClientId(CLIENT_ID)).thenReturn(GYM_ID);
        when(reservationRepository.findByEquipmentIdAndStatus(anyString(), any())).thenReturn(Optional.empty());
        when(reservationRepository.findByClientId(anyLong())).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static InitiateExpressReservation command(Time start, Time end) {
        return new InitiateExpressReservation(new ClientId(CLIENT_ID), new EquipmentId(EQUIPMENT_ID), start, end);
    }

    @SuppressWarnings("unchecked")
    private static ApplicationError errorOf(Result<Reservation, ApplicationError> result) {
        return ((Result.Failure<Reservation, ApplicationError>) assertInstanceOf(Result.Failure.class, result)).error();
    }

    @SuppressWarnings("unchecked")
    private static Reservation valueOf(Result<Reservation, ApplicationError> result) {
        return ((Result.Success<Reservation, ApplicationError>) assertInstanceOf(Result.Success.class, result)).value();
    }

    private static Reservation staleHold(Long clientId, String equipmentId) {
        return new Reservation(new InitiateExpressReservation(new ClientId(clientId), new EquipmentId(equipmentId), null, null),
                NOW.minusMinutes(15), 10);
    }

    @Test
    void freeEquipmentIsHeldForTenMinutes() {
        var reservation = valueOf(service.handle(command(null, null)));

        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(NOW, reservation.getStartedAt());
        assertEquals(NOW.plusMinutes(10), reservation.getHoldExpiresAt());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void sentTimesArePreserved() {
        var reservation = valueOf(service.handle(command(Time.valueOf("20:00:00"), Time.valueOf("21:00:00"))));

        assertEquals(Time.valueOf("20:00:00"), reservation.getTimeInterval().startTime());
        assertEquals(Time.valueOf("21:00:00"), reservation.getTimeInterval().endTime());
        assertEquals(NOW.plusMinutes(10), reservation.getHoldExpiresAt());
    }

    @Test
    void missingTimesAreDerived() {
        var reservation = valueOf(service.handle(command(null, null)));

        assertEquals(Time.valueOf("18:30:00"), reservation.getTimeInterval().startTime());
        assertEquals(Time.valueOf("18:40:00"), reservation.getTimeInterval().endTime());
    }

    @Test
    void invalidSentIntervalIsAValidationError() {
        var error = errorOf(service.handle(command(Time.valueOf("21:00:00"), Time.valueOf("20:00:00"))));

        assertEquals("VALIDATION_ERROR", error.code());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void unknownEquipmentIsNotFound() {
        when(gymContextFacade.findEquipmentById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("EQUIPMENT_NOT_FOUND", error.code());
    }

    @Test
    void equipmentOfAnotherGymIsForbidden() {
        when(gymContextFacade.resolveGymIdForEquipment(EQUIPMENT_ID)).thenReturn(Optional.of("other-gym"));

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("FORBIDDEN", error.code());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void equipmentWithoutResolvableGymIsForbidden() {
        when(gymContextFacade.resolveGymIdForEquipment(EQUIPMENT_ID)).thenReturn(Optional.empty());

        assertEquals("FORBIDDEN", errorOf(service.handle(command(null, null))).code());
    }

    @Test
    void clientWithoutActiveGymIsForbidden() {
        when(profilesContextFacade.fetchActiveGymIdByClientId(CLIENT_ID)).thenReturn("");

        assertEquals("FORBIDDEN", errorOf(service.handle(command(null, null))).code());
    }

    @ParameterizedTest
    @EnumSource(value = EquipmentStatus.class, names = {"OCCUPIED", "RESERVED"})
    void occupiedOrReservedEquipmentIsAConflict(EquipmentStatus status) {
        when(equipment.getStatus()).thenReturn(status);

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("RESERVATION_CONFLICT", error.code());
        assertEquals("Conflict with Reservation", error.message());
        assertTrue(error.details().toLowerCase().contains("equipment is already reserved"));
    }

    @ParameterizedTest
    @EnumSource(value = EquipmentStatus.class, names = {"MAINTENANCE", "OUT_OF_SERVICE", "DECOMMISSIONED"})
    void unavailableEquipmentIsABusinessRuleViolation(EquipmentStatus status) {
        when(equipment.getStatus()).thenReturn(status);

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("BUSINESS_RULE_VIOLATION", error.code());
        assertEquals("Business rule violation: Reservation", error.message());
        assertTrue(error.details().toLowerCase().contains("equipment is not available"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void clientWithAnActiveReservationIsAConflict() {
        when(reservationRepository.existsByClientIdAndStatus(CLIENT_ID, ReservationStatus.ACTIVE)).thenReturn(true);

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("RESERVATION_CONFLICT", error.code());
        assertTrue(error.details().toLowerCase().contains("client already has an active reservation"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void equipmentWithAnActiveReservationIsAConflict() {
        when(reservationRepository.existsByEquipmentIdAndStatus(EQUIPMENT_ID, ReservationStatus.ACTIVE)).thenReturn(true);

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("RESERVATION_CONFLICT", error.code());
        assertTrue(error.details().toLowerCase().contains("equipment is already reserved"));
    }

    @Test
    void concurrentHoldOnTheSameEquipmentLosesTheRace() {
        when(reservationRepository.save(any(Reservation.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(reservationRepository.existsByEquipmentIdAndStatus(EQUIPMENT_ID, ReservationStatus.ACTIVE)).thenReturn(false, true);

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("RESERVATION_CONFLICT", error.code());
        assertTrue(error.details().toLowerCase().contains("equipment is already reserved"));
    }

    @Test
    void concurrentHoldBySameClientLosesTheRace() {
        when(reservationRepository.save(any(Reservation.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(reservationRepository.existsByClientIdAndStatus(CLIENT_ID, ReservationStatus.ACTIVE)).thenReturn(false, true);

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("RESERVATION_CONFLICT", error.code());
        assertTrue(error.details().toLowerCase().contains("client already has an active reservation"));
    }

    @Test
    void staleHoldOnTheEquipmentIsExpiredLazilyAndTheReservationProceeds() {
        var stale = staleHold(9L, EQUIPMENT_ID);
        when(reservationRepository.findByEquipmentIdAndStatus(EQUIPMENT_ID, ReservationStatus.ACTIVE)).thenReturn(Optional.of(stale));

        var reservation = valueOf(service.handle(command(null, null)));

        assertEquals(ReservationStatus.EXPIRED, stale.getStatus());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        verify(reservationRepository).save(stale);
    }

    @Test
    void staleHoldOfTheSameClientDoesNotBlockANewReservation() {
        var stale = staleHold(CLIENT_ID, "eq-2");
        when(reservationRepository.findByClientId(CLIENT_ID)).thenReturn(List.of(stale));

        var reservation = valueOf(service.handle(command(null, null)));

        assertEquals(ReservationStatus.EXPIRED, stale.getStatus());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
    }

    @Test
    void holdStillRunningOnTheEquipmentIsNotExpired() {
        var running = new Reservation(new InitiateExpressReservation(new ClientId(9L), new EquipmentId(EQUIPMENT_ID), null, null),
                NOW.minusMinutes(2), 10);
        when(reservationRepository.findByEquipmentIdAndStatus(EQUIPMENT_ID, ReservationStatus.ACTIVE)).thenReturn(Optional.of(running));
        when(reservationRepository.existsByEquipmentIdAndStatus(EQUIPMENT_ID, ReservationStatus.ACTIVE)).thenReturn(true);

        var error = errorOf(service.handle(command(null, null)));

        assertEquals("RESERVATION_CONFLICT", error.code());
        assertEquals(ReservationStatus.ACTIVE, running.getStatus());
    }

    @Test
    void expireReservationExpiresAnElapsedHold() {
        var stale = staleHold(CLIENT_ID, EQUIPMENT_ID);
        when(reservationRepository.findByUuid(stale.getId().uuid())).thenReturn(Optional.of(stale));

        var expired = valueOf(service.handle(new ExpireReservation(stale.getId())));

        assertEquals(ReservationStatus.EXPIRED, expired.getStatus());
    }

    @Test
    void expireReservationRejectsAHoldThatHasNotElapsed() {
        var running = new Reservation(new InitiateExpressReservation(new ClientId(CLIENT_ID), new EquipmentId(EQUIPMENT_ID), null, null),
                NOW.minusMinutes(1), 10);
        when(reservationRepository.findByUuid(running.getId().uuid())).thenReturn(Optional.of(running));

        var error = errorOf(service.handle(new ExpireReservation(running.getId())));

        assertEquals("VALIDATION_ERROR", error.code());
        assertEquals(ReservationStatus.ACTIVE, running.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void expireReservationOfUnknownIdIsNotFound() {
        when(reservationRepository.findByUuid("nope")).thenReturn(Optional.empty());

        assertEquals("RESERVATION_NOT_FOUND", errorOf(service.handle(new ExpireReservation(new ReservationId("nope")))).code());
    }

    @Test
    void checkInAfterTheHoldElapsedIsRejected() {
        var stale = staleHold(CLIENT_ID, EQUIPMENT_ID);
        when(reservationRepository.findByUuid(stale.getId().uuid())).thenReturn(Optional.of(stale));

        var error = errorOf(service.handle(new StartReservationTimer(stale.getId(), 45)));

        assertEquals("VALIDATION_ERROR", error.code());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void checkInWithinTheHoldStartsTheTimerFromTheClock() {
        var running = new Reservation(new InitiateExpressReservation(new ClientId(CLIENT_ID), new EquipmentId(EQUIPMENT_ID), null, null),
                NOW.minusMinutes(3), 10);
        when(reservationRepository.findByUuid(running.getId().uuid())).thenReturn(Optional.of(running));

        var started = valueOf(service.handle(new StartReservationTimer(running.getId(), 45)));

        assertEquals(NOW.plusMinutes(45), started.getTimerExpiry());
    }
}
