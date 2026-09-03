package com.spottrack.platform.profiles.interfaces.rest;

import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.application.commandservices.ClientCommandService;
import com.spottrack.platform.profiles.application.queryservices.ClientQueryService;
import com.spottrack.platform.profiles.domain.model.commands.AssociateClientWithGymCommand;
import com.spottrack.platform.profiles.domain.model.commands.ChangeActiveGymCommand;
import com.spottrack.platform.profiles.domain.model.entities.ClientGymAssociation;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientGymAssociationsQuery;
import com.spottrack.platform.profiles.domain.model.valueobjects.ClientId;
import com.spottrack.platform.profiles.interfaces.rest.resources.AssociateGymResource;
import com.spottrack.platform.profiles.interfaces.rest.resources.ChangeActiveGymResource;
import com.spottrack.platform.profiles.interfaces.rest.resources.ClientGymAssociationResource;
import com.spottrack.platform.profiles.interfaces.rest.resources.ClientResource;
import com.spottrack.platform.profiles.interfaces.rest.resources.CreateClientResource;
import com.spottrack.platform.profiles.interfaces.rest.resources.UpdateClientProfileResource;
import com.spottrack.platform.profiles.interfaces.rest.transform.ClientResourceFromEntityAssembler;
import com.spottrack.platform.profiles.interfaces.rest.transform.CreateClientCommandFromResourceAssembler;
import com.spottrack.platform.profiles.interfaces.rest.transform.UpdateClientProfileCommandFromResourceAssembler;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/profiles/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clients", description = "Client profile management endpoints")
public class ClientsController {

    private final ClientCommandService clientCommandService;
    private final ClientQueryService clientQueryService;
    private final IamContextFacade iamContextFacade;

    public ClientsController(
            ClientCommandService clientCommandService,
            ClientQueryService clientQueryService,
            IamContextFacade iamContextFacade) {
        this.clientCommandService = clientCommandService;
        this.clientQueryService = clientQueryService;
        this.iamContextFacade = iamContextFacade;
    }

    @PostMapping
    @Operation(
            summary = "Create a new client",
            description = "Creates a new client profile with personal and contact information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Client created successfully",
                    content = @Content(schema = @Schema(implementation = ClientResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Conflict - client already exists")
    })
    public ResponseEntity<?> createClient(@Valid @RequestBody CreateClientResource resource) {
        var command = CreateClientCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = clientCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ClientResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get my client profile",
            description = "Retrieves the client profile of the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile found",
                    content = @Content(schema = @Schema(implementation = ClientResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        var optionalUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName());
        if (optionalUserId.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("User", authentication.getName()));
        }
        var query = new GetClientByUserIdQuery(optionalUserId.get());
        var client = clientQueryService.handle(query);
        if (client.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", "userId:" + optionalUserId.get()));
        }
        return ResponseEntity.ok(ClientResourceFromEntityAssembler.toResourceFromEntity(client.get()));
    }

    @PutMapping("/me")
    @Operation(
            summary = "Update my client profile",
            description = "Updates personal data (name, phone number, DNI) for the currently authenticated client."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = ClientResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<?> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateClientProfileResource resource) {
        var optionalUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName());
        if (optionalUserId.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("User", authentication.getName()));
        }
        var client = clientQueryService.handle(new GetClientByUserIdQuery(optionalUserId.get()));
        if (client.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", "userId:" + optionalUserId.get()));
        }
        var command = UpdateClientProfileCommandFromResourceAssembler
                .toCommandFromResource(client.get().getId(), resource);
        var result = clientCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ClientResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update client profile",
            description = "Updates personal data (name, phone number, DNI) for an existing client profile."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Client profile updated successfully",
                    content = @Content(schema = @Schema(implementation = ClientResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<?> updateClientProfile(
            @PathVariable
            @Parameter(description = "Client unique identifier", example = "1", required = true)
            Long clientId,
            @Valid @RequestBody UpdateClientProfileResource resource
    ) {
        var command = UpdateClientProfileCommandFromResourceAssembler.toCommandFromResource(clientId, resource);
        var result = clientCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ClientResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PostMapping("/me/gym-associations")
    public ResponseEntity<?> associateGym(Authentication authentication,
                                          @Valid @RequestBody AssociateGymResource resource) {
        var optionalUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName());
        if (optionalUserId.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("User", authentication.getName()));
        }
        var clientOpt = clientQueryService.handle(new GetClientByUserIdQuery(optionalUserId.get()));
        if (clientOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", "userId:" + optionalUserId.get()));
        }
        var command = new AssociateClientWithGymCommand(clientOpt.get().getId(), resource.gymId());
        var result = clientCommandService.handle(command);
        return switch (result) {
            case com.spottrack.platform.shared.application.result.Result.Success<ClientGymAssociation, ApplicationError> s ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(new ClientGymAssociationResource(s.value().getClientId(), s.value().getGymId(), s.value().isActive()));
            case com.spottrack.platform.shared.application.result.Result.Failure<ClientGymAssociation, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @GetMapping("/me/gym-associations")
    public ResponseEntity<?> getMyGymAssociations(Authentication authentication) {
        var optionalUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName());
        if (optionalUserId.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("User", authentication.getName()));
        }
        var clientOpt = clientQueryService.handle(new GetClientByUserIdQuery(optionalUserId.get()));
        if (clientOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", "userId:" + optionalUserId.get()));
        }
        var associations = clientQueryService.handle(new GetClientGymAssociationsQuery(clientOpt.get().getId()));
        var resources = associations.stream()
                .map(a -> new ClientGymAssociationResource(a.getClientId(), a.getGymId(), a.isActive()))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/me/active-gym")
    public ResponseEntity<?> changeActiveGym(Authentication authentication,
                                             @Valid @RequestBody ChangeActiveGymResource resource) {
        var optionalUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName());
        if (optionalUserId.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("User", authentication.getName()));
        }
        var clientOpt = clientQueryService.handle(new GetClientByUserIdQuery(optionalUserId.get()));
        if (clientOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", "userId:" + optionalUserId.get()));
        }
        var command = new ChangeActiveGymCommand(clientOpt.get().getId(), resource.gymId());
        var result = clientCommandService.handle(command);
        return switch (result) {
            case com.spottrack.platform.shared.application.result.Result.Success<ClientGymAssociation, ApplicationError> s ->
                    ResponseEntity.ok(new ClientGymAssociationResource(s.value().getClientId(), s.value().getGymId(), s.value().isActive()));
            case com.spottrack.platform.shared.application.result.Result.Failure<ClientGymAssociation, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get client by ID",
            description = "Retrieves a client profile by unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Client found",
                    content = @Content(schema = @Schema(implementation = ClientResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<?> getClientById(
            @PathVariable
            @Parameter(description = "Client unique identifier", example = "1", required = true)
            Long clientId
    ) {
        var query = new GetClientByIdQuery(new ClientId(clientId));
        var client = clientQueryService.handle(query);
        if (client.isEmpty()) {
            var error = ApplicationError.notFound("Client", clientId.toString());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }
        return ResponseEntity.ok(ClientResourceFromEntityAssembler.toResourceFromEntity(client.get()));
    }
}
