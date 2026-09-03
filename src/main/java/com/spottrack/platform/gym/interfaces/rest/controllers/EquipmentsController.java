package com.spottrack.platform.gym.interfaces.rest.controllers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.commands.DecomissionEquipment;
import com.spottrack.platform.gym.domain.model.commands.MarkEquipmentOutOfService;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentById;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.gym.interfaces.rest.resources.*;
import com.spottrack.platform.gym.interfaces.rest.transform.*;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/equipments")
@Tag(name = "Equipments")
public class EquipmentsController {

    private final EquipmentCommandService commandService;
    private final EquipmentQueryService equipmentQueryService;
    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;

    public EquipmentsController(EquipmentCommandService commandService,
                                EquipmentQueryService queryService,
                                GymContextFacade gymContextFacade,
                                IamContextFacade iamContextFacade) {
        this.commandService = commandService;
        this.equipmentQueryService = queryService;
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerEquipment(Authentication authentication,
                                               @RequestBody RegisterEquipmentResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var zoneOwnershipError = checkZoneOwnership(resource.zoneId(), adminUserId);
        if (zoneOwnershipError.isPresent()) return zoneOwnershipError.get();
        var command = RegisterEquipmentCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Equipment, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(EquipmentResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Equipment, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @GetMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEquipmentById(Authentication authentication,
                                              @PathVariable String equipmentId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkEquipmentOwnership(equipmentId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var equipment = equipmentQueryService.handle(new GetEquipmentById(new EquipmentId(equipmentId)));
        if (equipment.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(EquipmentResourceFromEntityAssembler.toResourceFromEntity(equipment.get()));
    }

    @PatchMapping("/{equipmentId}/out-of-service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> markEquipmentOutOfService(Authentication authentication,
                                                       @PathVariable String equipmentId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkEquipmentOwnership(equipmentId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new MarkEquipmentOutOfService(new EquipmentId(equipmentId));
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Equipment, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.OK)
                            .body(EquipmentResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Equipment, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @PatchMapping("/{equipmentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> UpdateEquipmentStatus(Authentication authentication,
                                                   @PathVariable String equipmentId,
                                                   @RequestBody UpdateEquipmentStatusResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkEquipmentOwnership(equipmentId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = UpdateEquipmentStatusCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Equipment, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.OK)
                            .body(EquipmentResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Equipment, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @PatchMapping("/{equipmentId}/relocate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> relocateEquipment(Authentication authentication,
                                               @PathVariable String equipmentId,
                                               @RequestBody RelocateEquipmentResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkEquipmentOwnership(equipmentId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var targetZoneError = checkZoneOwnership(resource.zoneId(), adminUserId);
        if (targetZoneError.isPresent()) return targetZoneError.get();
        var command = RelocateEquipmentCommandFromResourceAssembler.toCommandFromResource(equipmentId, resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Equipment, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.OK)
                            .body(EquipmentResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Equipment, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @PatchMapping("/{equipmentId}/decomission")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> decomissionEquipment(Authentication authentication,
                                                  @PathVariable String equipmentId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkEquipmentOwnership(equipmentId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new DecomissionEquipment(new EquipmentId(equipmentId), EquipmentStatus.DECOMMISSIONED);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Equipment, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.OK)
                            .body(EquipmentResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Equipment, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @PatchMapping("/{equipmentId}/maintenance-threshold")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> defineMaintenanceThreshold(Authentication authentication,
                                                        @PathVariable String equipmentId,
                                                        @RequestBody DefineMaintenanceThresholdResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkEquipmentOwnership(equipmentId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = DefineMaintenanceThresholdCommandFromResourceAssembler.toCommandFromResource(equipmentId, resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Equipment, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.OK)
                            .body(EquipmentResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Equipment, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @GetMapping({"", "/me"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllEquipments(Authentication authentication) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var resources = gymContextFacade.findEquipmentsByAdminUserId(adminUserId).stream()
                .map(EquipmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    private Long resolveAdminUserId(Authentication authentication) {
        return iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
    }

    private Optional<ResponseEntity<?>> checkZoneOwnership(String zoneId, Long adminUserId) {
        var gymId = gymContextFacade.resolveGymIdForZone(zoneId);
        if (gymId.isEmpty()) return Optional.of(ResponseEntity.notFound().build());
        if (!gymContextFacade.isGymOwnedByAdmin(gymId.get(), adminUserId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Equipment", "zoneId:" + zoneId)));
        }
        return Optional.empty();
    }

    private Optional<ResponseEntity<?>> checkEquipmentOwnership(String equipmentId, Long adminUserId) {
        var gymId = gymContextFacade.resolveGymIdForEquipment(equipmentId);
        if (gymId.isEmpty()) return Optional.of(ResponseEntity.notFound().build());
        if (!gymContextFacade.isGymOwnedByAdmin(gymId.get(), adminUserId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Equipment", "equipmentId:" + equipmentId)));
        }
        return Optional.empty();
    }
}
