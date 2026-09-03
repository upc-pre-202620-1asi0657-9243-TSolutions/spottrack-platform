package com.spottrack.platform.reservation.interfaces.rest.controllers;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.interfaces.rest.guards.GymMembershipAccessGuard;
import com.spottrack.platform.reservation.application.commandServices.ReservationRequestCommandService;
import com.spottrack.platform.reservation.application.queryservices.ReservationRequestQueryService;
import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;
import com.spottrack.platform.reservation.domain.model.commands.RequestEquipmentStatusChangeToAvailable;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationRequestByUuidQuery;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationRequestId;
import com.spottrack.platform.reservation.interfaces.rest.resources.AlternativeEquipmentResource;
import com.spottrack.platform.reservation.interfaces.rest.resources.RequestAlternativeEquipmentResource;
import com.spottrack.platform.reservation.interfaces.rest.resources.SubmitRequestOccupyEquipmentResource;
import com.spottrack.platform.reservation.interfaces.rest.transform.RequestAlternativeEquipmentCommandFromResourceAssembler;
import com.spottrack.platform.reservation.interfaces.rest.transform.ReservationRequestResourceFromEntityAssembler;
import com.spottrack.platform.reservation.interfaces.rest.transform.SubmitRequestOccupyEquipmentCommandFromResourceAssembler;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reservation-requests")
@Tag(name = "Reservation Requests")
public class ReservationRequestsController {

    private final ReservationRequestCommandService commandService;
    private final ReservationRequestQueryService queryService;
    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final GymContextFacade gymContextFacade;
    private final GymMembershipAccessGuard gymMembershipAccessGuard;

    public ReservationRequestsController(
            ReservationRequestCommandService commandService,
            ReservationRequestQueryService queryService,
            IamContextFacade iamContextFacade,
            ProfilesContextFacade profilesContextFacade,
            GymContextFacade gymContextFacade,
            GymMembershipAccessGuard gymMembershipAccessGuard) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.iamContextFacade = iamContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.gymContextFacade = gymContextFacade;
        this.gymMembershipAccessGuard = gymMembershipAccessGuard;
    }

    @PostMapping
    public ResponseEntity<?> submitRequest(
            Authentication authentication,
            @RequestBody SubmitRequestOccupyEquipmentResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membershipError = gymMembershipAccessGuard.check(clientId);
        if (membershipError.isPresent()) return membershipError.get();
        if (gymContextFacade.findEquipmentById(resource.equipmentId()).isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Equipment", resource.equipmentId()));
        }
        var command = SubmitRequestOccupyEquipmentCommandFromResourceAssembler.toCommandFromResource(resource, clientId);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<ReservationRequest, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(ReservationRequestResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<ReservationRequest, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PatchMapping("/{id}/alternative")
    public ResponseEntity<?> requestAlternative(
            Authentication authentication,
            @PathVariable String id,
            @RequestBody RequestAlternativeEquipmentResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membershipError = gymMembershipAccessGuard.check(clientId);
        if (membershipError.isPresent()) return membershipError.get();
        var request = queryService.handle(new GetReservationRequestByUuidQuery(id));
        if (request.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("ReservationRequest", id));
        }
        var ownershipError = checkOwnership(request.get().getClientId().clientId(), clientId, id);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = RequestAlternativeEquipmentCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<ReservationRequest, ApplicationError> s ->
                    ResponseEntity.ok(ReservationRequestResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<ReservationRequest, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @GetMapping("/{id}/alternatives")
    public ResponseEntity<?> viewAlternatives(Authentication authentication, @PathVariable String id) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var request = queryService.handle(new GetReservationRequestByUuidQuery(id));
        if (request.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("ReservationRequest", id));
        }
        var ownershipError = checkOwnership(request.get().getClientId().clientId(), clientId, id);
        if (ownershipError.isPresent()) return ownershipError.get();

        var requestedEquipmentId = request.get().getEquipmentId().uuid();
        var requestedEquipment = gymContextFacade.findEquipmentById(requestedEquipmentId);
        if (requestedEquipment.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Equipment", requestedEquipmentId));
        }
        var alternatives = gymContextFacade
                .findAvailableAlternatives(requestedEquipment.get().getEquipmentName(), requestedEquipmentId)
                .stream()
                .map(equipment -> new AlternativeEquipmentResource(
                        equipment.getId().uuid(), equipment.getEquipmentName(), equipment.getModel()))
                .toList();
        return ResponseEntity.ok(alternatives);
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<?> releaseEquipment(Authentication authentication, @PathVariable String id) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var request = queryService.handle(new GetReservationRequestByUuidQuery(id));
        if (request.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("ReservationRequest", id));
        }
        var ownershipError = checkOwnership(request.get().getClientId().clientId(), clientId, id);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new RequestEquipmentStatusChangeToAvailable(new ReservationRequestId(id));
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<ReservationRequest, ApplicationError> s ->
                    ResponseEntity.ok(ReservationRequestResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<ReservationRequest, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    private Long resolveClientId(Authentication authentication) {
        return profilesContextFacade.fetchClientIdByEmail(authentication.getName());
    }

    private Optional<ResponseEntity<?>> checkOwnership(Long requestClientId, Long callerClientId, String requestUuid) {
        if (!requestClientId.equals(callerClientId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("ReservationRequest", "requestId:" + requestUuid)));
        }
        return Optional.empty();
    }
}
