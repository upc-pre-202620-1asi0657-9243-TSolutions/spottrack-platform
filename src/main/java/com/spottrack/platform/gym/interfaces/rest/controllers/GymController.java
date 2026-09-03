package com.spottrack.platform.gym.interfaces.rest.controllers;

import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.commands.AddDniToWhitelistCommand;
import com.spottrack.platform.gym.domain.model.commands.RemoveDniFromWhitelistCommand;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.domain.model.entities.GymWhitelistEntry;
import com.spottrack.platform.gym.domain.model.entities.Zone;
import com.spottrack.platform.gym.domain.model.queries.GetAllGymsQuery;
import com.spottrack.platform.gym.domain.model.queries.GetBranchesByGymIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetGymById;
import com.spottrack.platform.gym.domain.model.queries.GetGymsByAdminUserId;
import com.spottrack.platform.gym.domain.model.queries.GetWhitelistByGymIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetZonesByGymIdQuery;
import com.spottrack.platform.gym.domain.model.valueobjects.Dni;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;
import com.spottrack.platform.gym.interfaces.rest.resources.AddBranchResource;
import com.spottrack.platform.gym.interfaces.rest.resources.AddDniToWhitelistResource;
import com.spottrack.platform.gym.interfaces.rest.resources.AddZoneResource;
import com.spottrack.platform.gym.interfaces.rest.resources.CreateGymResource;
import com.spottrack.platform.gym.interfaces.rest.resources.GymSummaryResource;
import com.spottrack.platform.gym.interfaces.rest.resources.WhitelistEntryResource;
import com.spottrack.platform.gym.interfaces.rest.resources.ZoneResource;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.gym.interfaces.rest.transform.*;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/gyms")
@Tag(name = "gyms")
public class GymController {

    private final GymCommandService commandService;
    private final GymQueryService gymQueryService;
    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final GymContextFacade gymContextFacade;

    public GymController(GymCommandService commandService,
                         GymQueryService gymQueryService,
                         IamContextFacade iamContextFacade,
                         ProfilesContextFacade profilesContextFacade,
                         GymContextFacade gymContextFacade) {
        this.commandService = commandService;
        this.gymQueryService = gymQueryService;
        this.iamContextFacade = iamContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.gymContextFacade = gymContextFacade;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createGym(Authentication authentication, @RequestBody CreateGymResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var command = CreateGymCommandFromResourceAssembler.toCommandFromResource(resource, adminUserId);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Gym, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED).body(GymResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Gym, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @GetMapping
    public ResponseEntity<List<GymSummaryResource>> getAllGyms() {
        // TODO: add pagination if gym volume grows
        var resources = gymQueryService.handle(new GetAllGymsQuery()).stream()
                .map(g -> new GymSummaryResource(g.getId().uuid(), g.getName()))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMyGyms(Authentication authentication) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        List<Gym> gyms = gymQueryService.handle(new GetGymsByAdminUserId(adminUserId));
        var resources = gyms.stream().map(GymResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{gymId}/branches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addBranch(Authentication authentication,
                                       @PathVariable String gymId,
                                       @RequestBody AddBranchResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkOwnership(gymId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = AddBranchCommandFromResourceAssembler.toCommandFromResource(gymId, resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Branch, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED).body(BranchResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Branch, ApplicationError> f when "BUSINESS_RULE_VIOLATION".equals(f.error().code()) ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body(f.error());
            case Result.Failure<Branch, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @GetMapping("/{gymId}/branches")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<?> getBranchesByGymId(Authentication authentication,
                                                 @PathVariable String gymId) {
        var accessError = checkGymAccess(authentication, gymId);
        if (accessError.isPresent()) return accessError.get();
        var branches = gymQueryService.handle(new GetBranchesByGymIdQuery(gymId));
        var resources = branches.stream()
                .map(BranchResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{gymId}/zones")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<?> getZonesByGymId(Authentication authentication,
                                              @PathVariable String gymId) {
        var accessError = checkGymAccess(authentication, gymId);
        if (accessError.isPresent()) return accessError.get();
        var zones = gymQueryService.handle(new GetZonesByGymIdQuery(gymId));
        List<ZoneResource> resources = zones.stream()
                .map(ZoneResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{gymId}/equipments")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<?> getEquipmentsByGymId(Authentication authentication,
                                                  @PathVariable String gymId) {
        var accessError = checkGymAccess(authentication, gymId);
        if (accessError.isPresent()) return accessError.get();
        var resources = gymContextFacade.findEquipmentsByGymId(gymId).stream()
                .map(EquipmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{gymId}/branches/{branchId}/zones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addZone(Authentication authentication,
                                     @PathVariable String gymId,
                                     @PathVariable String branchId,
                                     @RequestBody AddZoneResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkOwnership(gymId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var branches = gymQueryService.handle(new GetBranchesByGymIdQuery(gymId));
        if (branches.stream().noneMatch(b -> b.getId().uuid().equals(branchId))) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Branch", branchId));
        }
        var command = AddZoneCommandFromResourceAssembler.toCommandFromResource(branchId, resource);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<Zone, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED).body(ZoneResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<Zone, ApplicationError> f ->
                    ResponseEntity.badRequest().body(f.error());
        };
    }

    @PostMapping("/{gymId}/whitelist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addDniToWhitelist(Authentication authentication,
                                               @PathVariable String gymId,
                                               @RequestBody AddDniToWhitelistResource resource) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkOwnership(gymId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        Dni dni;
        try {
            dni = new Dni(resource.dni());
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("Dni", "gym.error.dni.invalidFormat"));
        }
        var command = new AddDniToWhitelistCommand(gymId, dni);
        var result = commandService.handle(command);
        return switch (result) {
            case Result.Success<GymWhitelistEntry, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(new WhitelistEntryResource(s.value().getGymId(), s.value().getDni().value()));
            case Result.Failure<GymWhitelistEntry, ApplicationError> f ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body(f.error());
        };
    }

    @DeleteMapping("/{gymId}/whitelist/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeDniFromWhitelist(Authentication authentication,
                                                    @PathVariable String gymId,
                                                    @PathVariable String dni) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkOwnership(gymId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        commandService.handle(new RemoveDniFromWhitelistCommand(gymId, dni));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{gymId}/whitelist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getWhitelist(Authentication authentication,
                                          @PathVariable String gymId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var ownershipError = checkOwnership(gymId, adminUserId);
        if (ownershipError.isPresent()) return ownershipError.get();
        var entries = gymQueryService.handle(new GetWhitelistByGymIdQuery(gymId));
        var resources = entries.stream()
                .map(e -> new WhitelistEntryResource(e.getGymId(), e.getDni().value()))
                .toList();
        return ResponseEntity.ok(resources);
    }

    private Long resolveAdminUserId(Authentication authentication) {
        return iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
    }

    private Optional<ResponseEntity<?>> checkGymAccess(Authentication authentication, String gymId) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            var adminUserId = resolveAdminUserId(authentication);
            if (adminUserId == 0L) {
                return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Admin", authentication.getName())));
            }
            return checkOwnership(gymId, adminUserId);
        }
        return checkClientAssociation(gymId, authentication.getName());
    }

    private Optional<ResponseEntity<?>> checkClientAssociation(String gymId, String username) {
        var clientId = profilesContextFacade.fetchClientIdByEmail(username);
        if (clientId == 0L) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", username)));
        }
        if (!profilesContextFacade.hasActiveAssociationWithGym(clientId, gymId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Gym", "gymId:" + gymId)));
        }
        return Optional.empty();
    }

    private Optional<ResponseEntity<?>> checkOwnership(String gymId, Long callerAdminUserId) {
        var gym = gymQueryService.handle(new GetGymById(new GymId(gymId)));
        if (gym.isEmpty()) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Gym", gymId)));
        }
        var storedAdminUserId = gym.get().getAdminUserId();
        if (storedAdminUserId == null || !storedAdminUserId.equals(callerAdminUserId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Gym", "gymId:" + gymId)));
        }
        return Optional.empty();
    }
}
