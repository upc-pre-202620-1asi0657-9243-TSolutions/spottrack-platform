package com.spottrack.platform.reservation.interfaces.rest.controllers;

import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.interfaces.rest.guards.GymMembershipAccessGuard;
import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.application.queryservices.ReservationQueryService;
import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.commands.CancelReservation;
import com.spottrack.platform.reservation.domain.model.commands.StartReservationTimer;
import com.spottrack.platform.reservation.domain.model.queries.GetAllReservationsQuery;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationByUuidQuery;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationsByClientIdQuery;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.reservation.interfaces.rest.resources.InitiateExpressReservationResource;
import com.spottrack.platform.reservation.interfaces.rest.resources.StartReservationTimerResource;
import com.spottrack.platform.reservation.interfaces.rest.transform.EndReservationCommandFromResourceAssembler;
import com.spottrack.platform.reservation.interfaces.rest.transform.InitiateExpressReservationCommandFromResourceAssembler;
import com.spottrack.platform.reservation.interfaces.rest.transform.ReservationResourceFromEntityAssembler;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations")
public class ReservationsController {

    private final ReservationCommandService commandService;
    private final ReservationQueryService reservationQueryService;
    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final GymMembershipAccessGuard gymMembershipAccessGuard;

    public ReservationsController(
            ReservationCommandService commandService,
            ReservationQueryService reservationQueryService,
            IamContextFacade iamContextFacade,
            ProfilesContextFacade profilesContextFacade,
            GymMembershipAccessGuard gymMembershipAccessGuard) {
        this.commandService = commandService;
        this.reservationQueryService = reservationQueryService;
        this.iamContextFacade = iamContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.gymMembershipAccessGuard = gymMembershipAccessGuard;
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> initiateExpressReservation(
            Authentication authentication,
            @RequestBody InitiateExpressReservationResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membershipError = gymMembershipAccessGuard.check(clientId);
        if (membershipError.isPresent()) return membershipError.get();
        var command = InitiateExpressReservationCommandFromResourceAssembler.toCommandFromResource(resource, clientId);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Reservation, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(ReservationResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Reservation, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PatchMapping("/{id}/timer")
    public ResponseEntity<?> startTimer(
            Authentication authentication,
            @PathVariable String id,
            @RequestBody StartReservationTimerResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membershipError = gymMembershipAccessGuard.check(clientId);
        if (membershipError.isPresent()) return membershipError.get();
        var reservation = reservationQueryService.handle(new GetReservationByUuidQuery(id));
        if (reservation.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Reservation", id));
        }
        var ownershipError = checkOwnership(reservation.get().getClientId().clientId(), clientId, id);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new StartReservationTimer(new ReservationId(id), resource.durationMinutes());
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Reservation, ApplicationError> s ->
                    ResponseEntity.ok(ReservationResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Reservation, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(Authentication authentication, @PathVariable String id) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var reservation = reservationQueryService.handle(new GetReservationByUuidQuery(id));
        if (reservation.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Reservation", id));
        }
        var ownershipError = checkOwnership(reservation.get().getClientId().clientId(), clientId, id);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new CancelReservation(new ReservationId(id));
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Reservation, ApplicationError> s ->
                    ResponseEntity.ok(ReservationResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Reservation, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<?> endReservation(Authentication authentication, @PathVariable String id) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var reservation = reservationQueryService.handle(new GetReservationByUuidQuery(id));
        if (reservation.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Reservation", id));
        }
        var ownershipError = checkOwnership(reservation.get().getClientId().clientId(), clientId, id);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = EndReservationCommandFromResourceAssembler.toCommandFromResource(id);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Reservation, ApplicationError> s ->
                    ResponseEntity.ok(ReservationResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Reservation, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @GetMapping
    @Operation(
            summary = "Get my reservations",
            description = "Returns all reservations belonging to the authenticated client.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getMyReservations(Authentication authentication) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var list = reservationQueryService.handle(new GetReservationsByClientIdQuery(clientId));
        var resources = list.stream()
                .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all reservations",
            description = "Admin-only: returns every reservation, so an admin can look one up without knowing its id.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getAllReservations() {
        var list = reservationQueryService.handle(new GetAllReservationsQuery());
        var resources = list.stream()
                .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    private Long resolveClientId(Authentication authentication) {
        return profilesContextFacade.fetchClientIdByEmail(authentication.getName());
    }

    private Optional<ResponseEntity<?>> checkOwnership(Long reservationClientId, Long callerClientId, String reservationUuid) {
        if (!reservationClientId.equals(callerClientId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Reservation", "reservationId:" + reservationUuid)));
        }
        return Optional.empty();
    }
}
